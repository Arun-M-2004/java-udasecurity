package com.udacity.catpoint.service;

import com.udacity.catpoint.data.*;
import com.udacity.catpoint.image.service.ImageService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    private SecurityService securityService;
    private Sensor sensor;

    @Mock
    private ImageService imageService;

    @Mock
    private SecurityRepository securityRepository;

    @BeforeEach
    void init() {
        securityService = new SecurityService(securityRepository, imageService);
        sensor = new Sensor("Test Sensor", SensorType.DOOR);
    }

    //  1: Armed + sensor activated → PENDING_ALARM
    @Test
    void armedSystem_sensorActivated_setPendingAlarm() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    // 2: Armed + sensor activated + already PENDING → ALARM
    @Test
    void armedSystem_sensorActivated_alreadyPending_setAlarm() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    // 3: PENDING alarm + all sensors inactive → NO_ALARM
    @Test
    void pendingAlarm_allSensorsInactive_setNoAlarm() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, false);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // 4: Alarm active → sensor change does NOT affect alarm
    @Test
    void alarmActive_sensorStateChange_noAlarmChange() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    // 5: Sensor already active + activated again + PENDING → ALARM
    @Test
    void sensorAlreadyActive_activatedAgain_pendingState_setAlarm() {
        sensor.setActive(true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    // 6: Sensor already inactive + deactivated → no alarm change
    @Test
    void sensorAlreadyInactive_deactivated_noAlarmChange() {
        sensor.setActive(false);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor, false);
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    // 7: Camera shows cat + system ARMED_HOME → ALARM
    @Test
    void cameraShowsCat_systemArmedHome_setAlarm() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        securityService.processImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    // 8: Camera shows no cat + no active sensors → NO_ALARM
    @Test
    void cameraShowsNoCat_noActiveSensors_setNoAlarm() {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        when(securityRepository.getSensors()).thenReturn(new HashSet<>());
        securityService.processImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // 9: System disarmed → NO_ALARM
    @Test
    void systemDisarmed_setNoAlarm() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // 10: System armed → all sensors reset to inactive
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void systemArmed_allSensorsResetToInactive(ArmingStatus armingStatus) {
        Sensor s1 = new Sensor("Door", SensorType.DOOR);
        Sensor s2 = new Sensor("Window", SensorType.WINDOW);
        s1.setActive(true);
        s2.setActive(true);
        Set<Sensor> sensors = new HashSet<>();
        sensors.add(s1);
        sensors.add(s2);
        when(securityRepository.getSensors()).thenReturn(sensors);
        securityService.setArmingStatus(armingStatus);
        sensors.forEach(s -> Assertions.assertFalse(s.getActive()));
    }

    // 11: System armed-home + cat already detected → ALARM
    @Test
    void systemArmedHome_catAlreadyDetected_setAlarm() {
        // Step 1: detect cat while disarmed (so cat flag is stored but no alarm yet)
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        when(securityRepository.getSensors()).thenReturn(new HashSet<>());
        securityService.processImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        // Step 2: arm home → should trigger ALARM because cat was detected
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }
}

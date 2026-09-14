package com.udacity.catpoint.service;

import com.udacity.catpoint.application.StatusListener;
import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;
import com.udacity.catpoint.image.service.ImageService;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

/**
 * Service that receives information about changes to the security system. Responsible for
 * forwarding updates to the repository and making any decisions about changing the system state.
 *
 * This is the class that should contain most of the business logic for our system, and it is the
 * class you will be writing unit tests for.
 */
public class SecurityService {

    private ImageService imageService;
    private SecurityRepository securityRepository;
    private Set<StatusListener> statusListeners = new HashSet<>();
    private boolean catDetectedFlag = false;

    public SecurityService(SecurityRepository securityRepository,
                           ImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    /**
     * Sets the current arming status for the system. Changing the arming status
     * may update both the alarm status.
     *
     * Req 9:  DISARMED → NO_ALARM
     * Req 10: ARMED    → reset all sensors to inactive
     * Req 11: ARMED_HOME + cat already detected → ALARM
     */
    public void setArmingStatus(ArmingStatus armingStatus) {
        if (armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        } else {
            // Req 10: reset all sensors to inactive and persist each one
            getSensors().forEach(sensor -> {
                sensor.setActive(false);
                securityRepository.updateSensor(sensor);
            });
        }
        securityRepository.setArmingStatus(armingStatus);
        // Req 11: if switching to ARMED_HOME and a cat was previously detected → ALARM
        if (armingStatus == ArmingStatus.ARMED_HOME && catDetectedFlag) {
            setAlarmStatus(AlarmStatus.ALARM);
        }
    }

    /**
     * Internal method that handles alarm status changes based on whether
     * the camera currently shows a cat.
     *
     * Req 7:  cat + ARMED_HOME → ALARM
     * Req 8:  no cat + no active sensors → NO_ALARM
     * Req 11: cat flag stored for use in setArmingStatus
     */
    private void catDetected(Boolean cat) {
        catDetectedFlag = cat;

        if (cat && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM);
        }

        if (!cat) {
            boolean allSensorsInactive =
                    getSensors().stream().noneMatch(Sensor::getActive);
            if (allSensorsInactive) {
                setAlarmStatus(AlarmStatus.NO_ALARM);
            }
        }

        statusListeners.forEach(sl -> sl.catDetected(cat));
    }

    /**
     * Register the StatusListener for alarm system updates from within the SecurityService.
     */
    public void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(StatusListener statusListener) {
        statusListeners.remove(statusListener);
    }

    /**
     * Change the alarm status of the system and notify all listeners.
     */
    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(sl -> sl.notify(status));
    }

    /**
     * Internal method for updating the alarm status when a sensor has been activated.
     *
     * Req 1: armed + NO_ALARM  → PENDING_ALARM
     * Req 2: armed + PENDING   → ALARM
     */
    private void handleSensorActivated() {
        if (securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return; // Req 9: no problem if the system is disarmed
        }
        switch (securityRepository.getAlarmStatus()) {
            case NO_ALARM      -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.ALARM);
            default            -> {}
        }
    }

    /**
     * Internal method for updating the alarm status when a sensor has been deactivated.
     *
     * Req 3: PENDING + all inactive → NO_ALARM
     * Req 4: ALARM → no change
     */
    private void handleSensorDeactivated() {
        switch (securityRepository.getAlarmStatus()) {
            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.NO_ALARM);
            case ALARM         -> {} // Req 4: alarm active, sensor changes ignored
            case NO_ALARM      -> {}
        }
    }

    /**
     * Change the activation status for the specified sensor and update alarm status if necessary.
     *
     * Req 4: alarm ACTIVE → sensor changes do NOT affect alarm state
     * Req 5: sensor already active + activated again + PENDING → ALARM
     * Req 6: sensor already inactive + deactivated → no alarm change
     */
    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
        // Req 4: if alarm is active, update sensor state but do NOT change alarm
        if (securityRepository.getAlarmStatus() == AlarmStatus.ALARM) {
            sensor.setActive(active);
            securityRepository.updateSensor(sensor);
            return;
        }

        if (sensor.getActive() && active) {
            // Req 5: sensor already active, activated again while PENDING → ALARM
            if (securityRepository.getAlarmStatus() == AlarmStatus.PENDING_ALARM) {
                setAlarmStatus(AlarmStatus.ALARM);
            }
        } else if (!sensor.getActive() && active) {
            // Req 1 / 2: sensor newly activated
            handleSensorActivated();
        } else if (sensor.getActive() && !active) {
            // Req 3: sensor deactivated
            handleSensorDeactivated();
        }
        // Req 6: !sensor.getActive() && !active → do nothing (falls through)

        sensor.setActive(active);
        securityRepository.updateSensor(sensor);
    }

    /**
     * Send an image to the SecurityService for processing. The securityService will use its
     * provided ImageService to analyze the image for cats and update the alarm status accordingly.
     */
    public void processImage(BufferedImage currentCameraImage) {
        catDetected(imageService.imageContainsCat(currentCameraImage, 50.0f));
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }
}

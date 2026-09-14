# UdaSecurity – Java Home Security Application

UdaSecurity is a Java desktop home-security application developed as part of the Udacity Java Application Deployment course. The application monitors security sensors, processes camera images, detects potential threats, and provides a graphical interface for controlling the security system.

## My Contribution

I implemented and tested the required functionality of the UdaSecurity application, including the security service logic, sensor management, image-based security monitoring, and user-interface behavior.

I worked primarily with Java, Maven, unit testing, and the existing CatPoint application architecture, and verified the implementation through automated tests and a successful Udacity project submission.

## Key Features

- Home security system monitoring
- Security system status management
- Sensor management and activation/deactivation
- Sensor status tracking
- Image-based threat detection
- Security alarm handling
- Automatic security status updates
- Graphical user interface for system control
- Unit and integration testing

## Technologies Used

- **Java**
- **Maven**
- **JUnit**
- **Java Swing**
- **Java Modules**
- **Object-Oriented Programming**
- **Unit Testing**
- **Integration Testing**

## Main Components

### SecurityService

The `SecurityService` manages the core security logic of the application.

Responsibilities include:

- Processing sensor events
- Managing alarm states
- Updating the security system status
- Responding to sensor activation/deactivation
- Processing image recognition results

### ControlPanel

The `ControlPanel` provides the main interface for controlling the security system.

It allows users to:

- Change the system security status
- View the current system state
- Manage connected sensors
- Monitor security events

### SensorPanel

The `SensorPanel` provides functionality for managing security sensors.

Users can:

- Add sensors
- Remove sensors
- Activate sensors
- Deactivate sensors
- View sensor status

### ImagePanel

The `ImagePanel` handles the camera/image side of the application and displays the results of image-based security monitoring.

## Testing

I used automated tests to verify the application's behavior and ensure that the implemented functionality works correctly.

The project was successfully built and tested using Maven.

```bash
mvn clean test

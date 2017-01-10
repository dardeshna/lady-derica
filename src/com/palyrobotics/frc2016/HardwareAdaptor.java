package com.palyrobotics.frc2016;

import com.palyrobotics.frc2016.Robot.RobotName;
import com.palyrobotics.frc2016.subsystems.Breacher;
import com.palyrobotics.frc2016.subsystems.DericaShooter;
import com.palyrobotics.frc2016.subsystems.Drive;
import com.palyrobotics.frc2016.subsystems.Intake;
import com.palyrobotics.frc2016.subsystems.LowGoalShooter;
import com.palyrobotics.frc2016.subsystems.TyrShooter;
import com.palyrobotics.frc2016.util.TalonEncoder;
import com.palyrobotics.frc2016.util.XboxController;
import com.team254.lib.util.CheesySpeedController;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.VictorSP;

public class HardwareAdaptor {
	/* 
	 * DRIVETRAIN
	 */
	public static Drive kDrive = null;

	static CANTalon kLeftDriveBackMotor  = null;
	static CANTalon kLeftDriveFrontMotor  = null;
	static CANTalon kRightDriveBackMotor  = null;
	static CANTalon kRightDriveFrontMotor  = null;

	static TalonEncoder kLeftDriveEncoder = null;
	static TalonEncoder kRightDriveEncoder = null;
	static ADXRS450_Gyro kGyro;
	static DoubleSolenoid kShifterSolenoid = null;

	// Instantiate drive motors
	static {
		kLeftDriveBackMotor = new CANTalon(Constants.kDericaLeftDriveBackMotorDeviceID);
		kLeftDriveBackMotor.enableBrakeMode(true);
		kLeftDriveFrontMotor = new CANTalon(Constants.kDericaLeftDriveFrontMotorDeviceID);
		kLeftDriveFrontMotor.enableBrakeMode(true);
		kRightDriveBackMotor = new CANTalon(Constants.kDericaRightDriveBackMotorDeviceID);
		kRightDriveBackMotor.enableBrakeMode(true);
		kRightDriveFrontMotor = new CANTalon(Constants.kDericaRightDriveFrontMotorDeviceID);
		kRightDriveFrontMotor.enableBrakeMode(true);
		
		kLeftDriveEncoder = new TalonEncoder(kLeftDriveBackMotor);
		kRightDriveEncoder = new TalonEncoder(kRightDriveBackMotor);

			
		kDrive = new Drive("drive", kLeftDriveBackMotor, kLeftDriveFrontMotor, kRightDriveBackMotor, kRightDriveFrontMotor, kLeftDriveEncoder, kRightDriveEncoder, kGyro, kShifterSolenoid);
	}

	/*
	 * INTAKE
	 */
	public static Intake kIntake = null;
	static {
		if(Robot.name == RobotName.TYR) {
			CheesySpeedController kLeftIntakeMotor = new CheesySpeedController(
					new VictorSP(Constants.kTyrLeftIntakeMotorDeviceID),
					Constants.kTyrLeftIntakeMotorPDP);
			CheesySpeedController kRightIntakeMotor = new CheesySpeedController(
					new VictorSP(Constants.kTyrRightIntakeMotorDeviceID),
					Constants.kTyrRightIntakeMotorPDP);
			// null for lack of a potentiometer
			kIntake = new Intake("intake", kLeftIntakeMotor, kRightIntakeMotor, null);
			System.out.println("Intake initialized");
		} else if (Robot.name == RobotName.DERICA) {
			CheesySpeedController kIntakeMotor = new CheesySpeedController(
					new CANTalon(Constants.kDericaIntakeMotorPWM),
					Constants.kDericaIntakeMotorPDP);
			CheesySpeedController kIntakeArmMotor = new CheesySpeedController(
					new CANTalon(Constants.kDericaArmIntakeMotorPWM),
					Constants.kDericaArmIntakeMotorPDP);
			AnalogPotentiometer kArmPotentiometer = null;
			kIntake = new Intake("intake", kIntakeMotor, kIntakeArmMotor, kArmPotentiometer);
		}
	}
	
	/*
	 * SHOOTER/CATAPULT
	 * TyrShooter comes with Grabber
	 */

	// Pneumatic solenoids, only instantiate if Tyr
	static DoubleSolenoid kShooterSolenoid = null;
	static DoubleSolenoid kLatchSolenoid = null;
	static DoubleSolenoid kGrabberSolenoid = null;
	static CheesySpeedController kShooterMotor = null;
	
	public static TyrShooter kTyrShooter = null;
    public static DericaShooter kCatapult = null;
    public static LowGoalShooter kLowGoalShooter = null;

	static {
		if(Robot.name == RobotName.TYR){
			kShooterSolenoid = new DoubleSolenoid(
					Constants.kShooterSolenoidPortExtend, Constants.kShooterSolenoidPortRetract);
			kLatchSolenoid = new DoubleSolenoid(
					Constants.kLatchSolenoidPortExtend, Constants.kLatchSolenoidPortRetract);
			kGrabberSolenoid = new DoubleSolenoid(
					Constants.kGrabberSolenoidPortExtend, Constants.kGrabberSolenoidPortRetract);
			kShooterMotor = new CheesySpeedController(new CANTalon(Constants.kTyrShooterMotorDeviceID), 
					Constants.kTyrShooterMotorPDP);
			kTyrShooter = new TyrShooter("shooter", kShooterMotor, kShooterSolenoid, kLatchSolenoid, kGrabberSolenoid);
		} else {
			kCatapult = new DericaShooter("catapult", null, null, null, null, null);
			kLowGoalShooter = new LowGoalShooter("low goal shooter", new CheesySpeedController(
		    		new Victor(Constants.kDericaLowGoalShooterPWM), Constants.kDericaLowGoalShooterPDP));
		}
	}

	/*
	 * BREACHER
	 */
	public static Breacher kBreacher = null;
	public static CheesySpeedController kBreacherMotor = null;
	
	static {
		if(Robot.name == RobotName.TYR) {
			kBreacherMotor = new CheesySpeedController(new CANTalon(Constants.kBreacherMotorDeviceID), Constants.kBreacherMotorPDP);
			kBreacher = new Breacher("breacher", kBreacherMotor);
		}
	}
	
	public static PowerDistributionPanel kPDP = new PowerDistributionPanel();

	// Compressor
	//    public static Relay kCompressorRelay = new Relay(Constants.kCompressorRelayPort);
	//    public static DigitalInput kCompressorSwitch = new DigitalInput(Constants.kPressureSwitchDIO);
	//    public static CheesyCompressor kCompressor = new CheesyCompressor(kCompressorRelay, kCompressorSwitch);
	
	// Operator Interface
	public static Joystick kLeftStick = new Joystick(0);
	public static Joystick kRightStick = new Joystick(1);
	public static Joystick kOperatorStick;
	static{
		if(Robot.name == RobotName.TYR) {
			kOperatorStick = new XboxController(2);
		}
		else {
			kOperatorStick = new Joystick(2);
		}
	}
}
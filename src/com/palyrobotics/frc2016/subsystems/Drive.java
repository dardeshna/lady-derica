package com.palyrobotics.frc2016.subsystems;

import com.palyrobotics.frc2016.Constants;
import com.palyrobotics.frc2016.Robot;
import com.palyrobotics.frc2016.Robot.RobotName;
import com.palyrobotics.frc2016.subsystems.controllers.EncoderTurnAngleController;
import com.palyrobotics.frc2016.subsystems.controllers.GyroTurnAngleController;
import com.palyrobotics.frc2016.subsystems.controllers.team254.DriveFinishLineController;
import com.palyrobotics.frc2016.subsystems.controllers.team254.DrivePathController;
import com.palyrobotics.frc2016.subsystems.controllers.team254.DriveStraightController;
import com.palyrobotics.frc2016.subsystems.controllers.team254.TurnInPlaceController;
import com.palyrobotics.frc2016.util.TalonEncoder;
import com.team254.lib.trajectory.Path;
import com.team254.lib.util.DriveSignal;
import com.team254.lib.util.Loop;
import com.team254.lib.util.Pose;
import com.team254.lib.util.StateHolder;
import com.team254.lib.util.Subsystem;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;

public class Drive extends Subsystem implements Loop {

	public interface DriveController {
		DriveSignal update(Pose pose);

		Pose getCurrentSetpoint();

		public boolean onTarget();

	}
	private DoubleSolenoid m_shifter_solenoid = null;
	private CANTalon m_left_back_motor;
	private CANTalon m_left_front_motor;
	private CANTalon m_right_back_motor;
	private CANTalon m_right_front_motor;
	
	private CANTalon m_left_motor;
	private CANTalon m_right_motor;
	
	protected TalonEncoder m_left_encoder;
	protected TalonEncoder m_right_encoder;
	protected ADXRS450_Gyro m_gyro;
	private DriveController m_controller = null;
	
	// Derica is always considered high gear
	public enum DriveGear {HIGH, LOW}
	public DriveGear mGear;
	
	// Encoder DPP
	protected final double m_inches_per_tick;

	protected final double m_wheelbase_width; // Get from CAD
	protected final double m_turn_slip_factor; // Measure empirically
	private Pose m_cached_pose = new Pose(0, 0, 0, 0, 0, 0); // Don't allocate poses at 200Hz!

	public Drive(String name, CANTalon leftBackMotor, CANTalon leftFrontMotor, CANTalon rightBackMotor, CANTalon rightFrontMotor, TalonEncoder left_encoder,
			TalonEncoder right_encoder, ADXRS450_Gyro gyro, DoubleSolenoid shifter_solenoid) {
		super(name);
		if(Robot.name == RobotName.TYR) {
			m_wheelbase_width = 26.0;
			m_turn_slip_factor = 1.2;
			// TODO: Encoder DPP's
			m_inches_per_tick = 0.184;
		}
		else {
			m_wheelbase_width = 22.0;
			m_turn_slip_factor = 1.2;
			// TODO: Encoder DPP's
			m_inches_per_tick = 0.07033622;
			mGear = DriveGear.HIGH;
		}
		
		this.m_left_back_motor = leftBackMotor;
		this.m_left_front_motor = leftFrontMotor;
		this.m_right_back_motor = rightBackMotor;
		this.m_right_front_motor = rightFrontMotor;
		
		m_left_front_motor.changeControlMode(TalonControlMode.Follower);
		m_left_front_motor.set(Constants.kDericaLeftDriveBackMotorDeviceID);
		m_right_front_motor.changeControlMode(TalonControlMode.Follower);
		m_right_front_motor.set(Constants.kDericaRightDriveBackMotorDeviceID);
		
		this.m_left_motor = m_left_back_motor;
		this.m_right_motor = m_right_back_motor;
		
		this.m_left_encoder = left_encoder;
		this.m_right_encoder = right_encoder;
		this.m_left_encoder.setDistancePerPulse(m_inches_per_tick);
		this.m_right_encoder.setDistancePerPulse(m_inches_per_tick);
		this.m_gyro = gyro;
		this.m_shifter_solenoid = shifter_solenoid;

	}

	public void setOpenLoop(DriveSignal signal) {
		m_controller = null;
		setDriveOutputs(signal);
		System.out.println("Left Distance: " + m_left_encoder.getDistance());
		System.out.println("Right Distance: " + m_right_encoder.getDistance());


	}
	
	public void setGear(DriveGear targetGear) {
		if(Robot.name == RobotName.DERICA) {
			System.err.println("No gear shifting on Derica");
			return;
		}
		switch(targetGear) {
			case HIGH:
				//TODO Which is high and which is low?
				m_shifter_solenoid.set(Value.kForward);
				break;
			case LOW:
				m_shifter_solenoid.set(Value.kReverse);
				break;
		}
	}
	
	public boolean isHighGear() {
		return mGear == DriveGear.HIGH;
	}

	public void setDistanceSetpoint(double distance) {
		setDistanceSetpoint(distance, Constants.kDriveMaxSpeedInchesPerSec);
	}

	public void setDistanceSetpoint(double distance, double velocity) {
		// 0 < vel < max_vel
		double vel_to_use = Math.min(Constants.kDriveMaxSpeedInchesPerSec, Math.max(velocity, 0));
		m_controller = new DriveStraightController(
				getPoseToContinueFrom(false),
				distance,
				vel_to_use);
	}
	
	public void setAutoAlignSetpoint(double heading) {
		// Check if already turning to that setpoint
		if(m_controller instanceof GyroTurnAngleController) {
//			if(m_controller.getCurrentSetpoint().getHeading()-getPhysicalPose().getHeading() != heading) {
//				// New auto align iteration
//				System.out.println("New auto align setpoint");
//				setGyroTurnAngleSetpoint(heading);
//			}
		} else {
			System.out.println("Started auto align controller");
			setGyroTurnAngleSetpoint(heading, 0.45);
		}
	}
	
	public void setTurnSetpoint(double heading) {
		setTurnSetpoint(heading, Constants.kTurnMaxSpeedRadsPerSec);
	}

	public void setTurnSetpoint(double heading, double velocity) {
		velocity = Math.min(Constants.kTurnMaxSpeedRadsPerSec, Math.max(velocity, 0));
		m_controller = new TurnInPlaceController(getPoseToContinueFrom(true), heading, velocity);
	}

	public void setEncoderTurnAngleSetpoint(double heading) {
		setEncoderTurnAngleSetpoint(heading, 1);
	}
	public void setEncoderTurnAngleSetpoint(double heading, double maxVel) {
		m_controller = new EncoderTurnAngleController(getPoseToContinueFrom(true), heading, maxVel);
	}
	
	public void setGyroTurnAngleSetpoint(double heading) {
		setGyroTurnAngleSetpoint(heading, 0.7);
	}
	public void setGyroTurnAngleSetpoint(double heading, double maxVel) {
		m_controller = new GyroTurnAngleController(getPoseToContinueFrom(true), heading, maxVel);
	}
	
	public void reset() {
		m_left_encoder.reset();
		m_right_encoder.reset();
		m_controller = null;
	}

	public void setPathSetpoint(Path path) {
		reset();
		m_controller = new DrivePathController(path);
	}

	public void setFinishLineSetpoint(double distance, double heading) {
		reset();
		m_controller = new DriveFinishLineController(distance, heading, 1.0);
	}

	@Override
	public void getState(StateHolder states) {
		//        states.put("gyro_angle", m_gyro.getAngle());
		states.put("left_encoder", m_left_encoder.getDistance());
		states.put("left_encoder_rate", m_left_encoder.getRate());
		states.put("right_encoder_rate", m_right_encoder.getRate());
		states.put("right_encoder", m_right_encoder.getDistance());

		Pose setPointPose = m_controller == null ? getPhysicalPose(): m_controller.getCurrentSetpoint();
				states.put("drive_set_point_pos",
					DriveStraightController.encoderDistance(setPointPose));
				states.put("turn_set_point_pos", setPointPose.getHeading());
				states.put("left_signal", m_left_motor.get());
				states.put("right_signal", m_right_motor.get());
				states.put("on_target", (m_controller != null && m_controller.onTarget()) ? 1.0 : 0.0);
	}

	@Override
	public void onStart() {
	}
	@Override
	public void onStop() {
	}
	@Override
	public void onLoop() {
		if(!hasController()) {
			return;
		}
		setDriveOutputs(m_controller.update(getPhysicalPose()));
	}

	private void setDriveOutputs(DriveSignal signal) {
		m_left_motor.set(signal.leftMotor);
		m_right_motor.set(-signal.rightMotor);
	}

	private Pose getPoseToContinueFrom(boolean for_turn_controller) {
		if (!for_turn_controller && m_controller instanceof TurnInPlaceController) {
			Pose pose_to_use = getPhysicalPose();
			pose_to_use.m_heading = ((TurnInPlaceController) m_controller).getHeadingGoal();
			pose_to_use.m_heading_velocity = 0;
			return pose_to_use;
		} else if (m_controller == null || (m_controller instanceof DriveStraightController && for_turn_controller)) {
			return getPhysicalPose();
		} else if (m_controller instanceof DriveFinishLineController) {
			return getPhysicalPose();
		} else if (m_controller.onTarget()) {
			return m_controller.getCurrentSetpoint();
		} else {
			return getPhysicalPose();
		}
	}

	/**
	 * @return The pose according to the current sensor state
	 */
	public Pose getPhysicalPose() {
		m_cached_pose.reset(
				m_left_encoder.getDistance(),
				m_right_encoder.getDistance(),
				m_left_encoder.getRate(),
				m_right_encoder.getRate(),
				m_gyro.getAngle(),
				m_gyro.getRate());
		return m_cached_pose;
	}

	public Drive.DriveController getController() {
		return m_controller;
	}

	@Override
	public void reloadConstants() {
		// TODO Auto-generated method stub

	}

	public boolean controllerOnTarget() {
		return m_controller != null && m_controller.onTarget();
	}

	public boolean hasController() {
		return m_controller != null;
	}
}
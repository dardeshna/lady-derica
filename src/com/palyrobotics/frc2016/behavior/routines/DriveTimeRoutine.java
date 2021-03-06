package com.palyrobotics.frc2016.behavior.routines;

import java.util.Optional;

import com.palyrobotics.frc2016.HardwareAdaptor;
import com.palyrobotics.frc2016.behavior.Commands;
import com.palyrobotics.frc2016.behavior.RobotSetpoints;
import com.palyrobotics.frc2016.subsystems.Drive;
import com.team254.lib.util.DriveSignal;

import edu.wpi.first.wpilibj.Timer;

public class DriveTimeRoutine extends Routine {

	private enum DriveTimeRoutineStates {
		START, DRIVING, DONE
	}

	DriveTimeRoutineStates m_state = DriveTimeRoutineStates.START;
	Timer m_timer = new Timer();
	// Default values for time and velocity setpoints
	private double m_time_setpoint;
	private double m_velocity_setpoint;
	
	private boolean m_is_new_state = true;

	private Drive drive = HardwareAdaptor.kDrive;
		
	/**
	 * Constructs with a specified time setpoint and velocity
	 * @param time How long to drive (seconds)
	 * @param velocity What velocity to drive at (0 to 1)
	 */
	public DriveTimeRoutine(double time, double velocity) {
		setTimeSetpoint(time);
		setVelocity(velocity);
	}
	
	/**
	 * Sets the time setpoint that will be used
	 * @param time how long to drive forward in seconds
	 */
	public void setTimeSetpoint(double time) {
		this.m_time_setpoint = time;
	}
	
	/**
	 * Sets the velocity setpoint
	 * @param velocity target velocity to drive at (0 to 1)
	 * @return true if valid setspeed
	 */
	public boolean setVelocity(double velocity) {
		if(velocity > 0) {
			this.m_velocity_setpoint = velocity;
			return true;
		}
		return false;
	}
	//Routines just change the states of the robotsetpoints, which the behavior manager then moves the physical subsystems based on.
	@Override
	public RobotSetpoints update(Commands commands, RobotSetpoints existing_setpoints) {
		DriveTimeRoutineStates new_state = m_state;
		RobotSetpoints setpoints = existing_setpoints;
		switch (m_state) {
		case START:
			// Only set the setpoint the first time the state is START
			if(m_is_new_state) {
				m_timer.reset();
				m_timer.start();
				setpoints.timer_drive_time_setpoint = RobotSetpoints.m_nullopt;
				setpoints.drive_velocity_setpoint = RobotSetpoints.m_nullopt;
			}

			setpoints.drive_routine_action = RobotSetpoints.DriveRoutineAction.TIMER_DRIVE;
			new_state = DriveTimeRoutineStates.DRIVING;
			break;
		case DRIVING:
			setpoints.timer_drive_time_setpoint = Optional.of(m_time_setpoint);
			setpoints.drive_velocity_setpoint = Optional.of(m_velocity_setpoint);
			if(m_timer.get() >= m_time_setpoint) {
//				setpoints.timer_drive_time_setpoint = RobotSetpoints.m_nullopt;
//				setpoints.drive_velocity_setpoint = RobotSetpoints.m_nullopt;
//				cancel();
				new_state = DriveTimeRoutineStates.DONE;
			}
			break;
		case DONE:
			drive.reset();
			System.out.println("DONE called");
			setpoints.drive_routine_action = RobotSetpoints.DriveRoutineAction.NONE;
			setpoints.timer_drive_time_setpoint = RobotSetpoints.m_nullopt;
			setpoints.drive_velocity_setpoint = RobotSetpoints.m_nullopt;
			break;
		}

		m_is_new_state = false;
		if(new_state != m_state) {
			m_state = new_state;
			//m_timer.reset();
			m_is_new_state = true;
		}
		
		return setpoints;
	}

	@Override
	public void cancel() {
		m_state = DriveTimeRoutineStates.DONE;
		System.out.println("Cancelling");
		m_timer.stop();
		m_timer.reset();
		drive.reset();
		drive.setOpenLoop(DriveSignal.NEUTRAL);
	}
	
	@Override
	public void start() {
		drive.reset();
		m_timer.reset();
		m_state = DriveTimeRoutineStates.START;
	}
	
	@Override
	public boolean isFinished() {
		// allow
		return m_state == DriveTimeRoutineStates.DONE && m_is_new_state==false;
	}

	@Override
	public String getName() {
		return "Timer Drive Forward";
	}

}

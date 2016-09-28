package com.palyrobotics.frc2016.behavior.routines;

import java.util.Optional;

import com.palyrobotics.frc2016.HardwareAdaptor;
import com.palyrobotics.frc2016.behavior.Commands;
import com.palyrobotics.frc2016.behavior.RobotSetpoints;
import com.palyrobotics.frc2016.subsystems.Drive;
import com.palyrobotics.lib.util.DriveSignal;

import edu.wpi.first.wpilibj.Timer;

public class TimerDriveRoutine extends Routine {

	public enum TimerDriveRoutineStates {
		START, DRIVING, DONE
	}

	TimerDriveRoutineStates m_state = TimerDriveRoutineStates.START;
	Timer m_timer = new Timer();
	// Default values for time and velocity setpoints
	private double m_time_setpoint = 3;
	private double m_velocity_setpoint = 0.5;
	
	private boolean m_is_new_state = true;

	private Drive drive = HardwareAdaptor.kDrive;
	
	/**
	 * Constructs with a specified time setpoint
	 * @param time How long to drive (seconds)
	 */
	public TimerDriveRoutine(double time) {
		this.m_time_setpoint = time;
	}
	
	/**
	 * Constructs with a specified time setpoint and velocity
	 * @param time How long to drive (seconds)
	 * @param velocity What velocity to drive at (0 to 1)
	 */
	public TimerDriveRoutine(double time, double velocity) {
		this.m_time_setpoint = time;
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
		TimerDriveRoutineStates new_state = m_state;
		RobotSetpoints setpoints = existing_setpoints;
		switch (m_state) {
		case START:
			m_timer.reset();
			m_timer.start();
			// Only set the setpoint the first time the state is START 
			if(m_is_new_state) {
				setpoints.timer_drive_time_setpoint = Optional.of(m_time_setpoint);
				setpoints.drive_velocity_setpoint = Optional.of(m_velocity_setpoint);
			}

			setpoints.drive_routine_action = RobotSetpoints.DriveRoutineAction.TIMER_DRIVE;
			new_state = TimerDriveRoutineStates.DRIVING;
			break;
		case DRIVING:
			setpoints.timer_drive_time_setpoint = Optional.of(m_time_setpoint);
			setpoints.drive_velocity_setpoint = Optional.of(m_velocity_setpoint);
			if(m_timer.get() > m_time_setpoint) {
				new_state = TimerDriveRoutineStates.DONE;
			}
			break;
		case DONE:
			drive.reset();
			setpoints.drive_routine_action = RobotSetpoints.DriveRoutineAction.NONE;
			break;
		}

		m_is_new_state = false;
		if(new_state != m_state) {
			m_state = new_state;
			m_timer.reset();
			m_is_new_state = true;
		}
		
		return setpoints;
	}

	@Override
	public void cancel() {
		m_state = TimerDriveRoutineStates.DONE;
		m_timer.stop();
		m_timer.reset();
		drive.setOpenLoop(DriveSignal.NEUTRAL);
		drive.reset();
	}
	
	@Override
	public void start() {
		drive.reset();
		m_timer.reset();
	}
	
	@Override
	public boolean isFinished() {
		return m_state == TimerDriveRoutineStates.DONE;
	}

	@Override
	public String getName() {
		return "Timer Drive Forward";
	}

}

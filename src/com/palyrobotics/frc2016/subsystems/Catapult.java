package com.palyrobotics.frc2016.subsystems;

import com.palyrobotics.frc2016.config.Commands;
import com.palyrobotics.frc2016.config.RobotState;
import com.palyrobotics.frc2016.util.Subsystem;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;

public class Catapult extends Subsystem {
	private static Catapult instance_ = new Catapult();
	public static Catapult getInstance() {
		return instance_;
	}
	// Stores output
	private double winchMotorOutput = 0.0;
	private DoubleSolenoid.Value lockSolenoidOutput;

	private Catapult() {
		super("Catapult");
	}

	/**
	 * TODO: this method and documentation
	 * @param commands
	 * @param robotState
	 */
	@Override
	public void update(Commands commands, RobotState robotState) {
		if (commands.winch_request == Commands.WinchRequest.WIND) {
			// Temporary speed, replace with a constant later
			wind(1);
		} else if (commands.winch_request == Commands.WinchRequest.UNWIND) {
			// Temporary speed, replace with a constant later
			unwind(-1);
		}

		// Parse pin commands because this is only open loop
		if (commands.pin_request == Commands.PinRequest.LOCK) {
			lock();
		} else if (commands.pin_request == Commands.PinRequest.UNLOCK) {
			unlock();
		}
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	// Hold the catapult in place
	public void lock() {
		lockSolenoidOutput = Value.kForward;
	}
	
	// Release the catapult
	public void unlock() {
		lockSolenoidOutput = Value.kReverse;
	}
	
	// Tighten the elastics
	public void wind(double d) {
		winchMotorOutput = d;
	}
	
	// Loosen the elastics
	public void unwind(double d) {
		winchMotorOutput = -d;
	}
	
	public DoubleSolenoid.Value getLockSolenoidOutput() {
		return lockSolenoidOutput;
	}
	
	public double getWinchMotorOutput() {
		return winchMotorOutput;
	}
}
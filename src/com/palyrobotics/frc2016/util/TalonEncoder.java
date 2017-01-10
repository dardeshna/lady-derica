package com.palyrobotics.frc2016.util;

import edu.wpi.first.wpilibj.CANTalon;

public class TalonEncoder {

	private CANTalon talon;
	private double dpp;
	private double offset;
	
	public TalonEncoder(CANTalon talon) {
		this.talon = talon;
	}
	
	public void setDistancePerPulse(double dpp) {
//		this.dpp = dpp;
		this.dpp = 1;
	}
	
	public double getDistance() {
		return (talon.getEncPosition()-offset)*dpp;
	}
	
	public double getRate() {
		return talon.getEncVelocity()*dpp;
		
	}
	
	public void reset() {
		this.offset = talon.getEncPosition();
	}
	
}

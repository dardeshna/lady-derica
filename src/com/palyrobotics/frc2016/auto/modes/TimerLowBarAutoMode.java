package com.palyrobotics.frc2016.auto.modes;

import java.util.ArrayList;

import com.palyrobotics.frc2016.Robot;
import com.palyrobotics.frc2016.Robot.RobotName;
import com.palyrobotics.frc2016.auto.AutoMode;
import com.palyrobotics.frc2016.auto.AutoModeEndedException;
import com.palyrobotics.frc2016.auto.actions.Action;
import com.palyrobotics.frc2016.auto.actions.DriveTimeAction;
import com.palyrobotics.frc2016.auto.actions.GetLowAction;
import com.palyrobotics.frc2016.auto.actions.IntakeAction;
import com.palyrobotics.frc2016.auto.actions.ParallelAction;
import com.palyrobotics.frc2016.subsystems.Intake.WantedIntakeState;

public class TimerLowBarAutoMode extends AutoMode {
	
	public static final double mCompressorWaitTime = 3;
	private double leftSpeed = 0.5;
	private double rightSpeed = 0.5;
	private double crossTime = 3.5;
	
	@Override
	protected void routine() throws AutoModeEndedException {
		//if tyr, wait for compressor
		if(Robot.name == RobotName.TYR) {
			waitTime(mCompressorWaitTime);
		}
		
		ArrayList<Action> crossLowBar = new ArrayList<Action>();
		
		//move across low bar
		crossLowBar.add(new DriveTimeAction(crossTime, -leftSpeed, -rightSpeed));
		
		//if tyr, move shooter down
		if(Robot.name == RobotName.TYR) {
			crossLowBar.add(new GetLowAction());
		} 
		//if derica, move intake down while crossing
		else {
//			crossLowBar.add(new IntakeAction(0.25, WantedIntakeState.LOWERING));
		}
		runAction(new ParallelAction(crossLowBar));
	}

	@Override
	public String toString() {
		return "TimerLowBarAutoMode";
	}

	@Override
	public void prestart() {
		drive.reset();
		
	}

}
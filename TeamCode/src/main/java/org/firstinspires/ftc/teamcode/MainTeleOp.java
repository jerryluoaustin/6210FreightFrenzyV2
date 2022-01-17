package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.HashMap;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name="TeleOp", group = "TeleOp")
public class MainTeleOp extends OpMode
{
    Manipulators manip;
    Robot robot;
    DcMotor IT;
    ElapsedTime waitTimer = new ElapsedTime();
    double wait = 3;

    boolean manual = true;
    boolean field = false;
    boolean retract = false;
    double zeroAng = 0;

    public HashMap<String, Boolean> buttons = new HashMap<String, Boolean>();
    double[] motorPower = {0, 0, 0, 0};

    public void init()
    {

        manip = new Manipulators(hardwareMap);
        robot = new Robot(hardwareMap);

        IT = hardwareMap.dcMotor.get("intake");

        telemetry.addData("init ", "completed");
        telemetry.update();
    }

    public boolean isPressed(String name, boolean button){
        boolean output = false;

        //If the hashmap doesn't already contain the key
        if (!buttons.containsKey(name)){
            buttons.put(name, false);
        }

        boolean buttonWas = buttons.get(name);
        if (button != buttonWas && button == true){
            output = true;
        }

        buttons.put(name, button);

        return output;
    }

    @Override
    public void loop()
    {

        // Auto arm variables
        // Encoder positions for each level
        double leftY = 0;
        double leftX = 0;
        double rightX = 0;

        if (isPressed("1y", gamepad1.y)) {
            robot.setHalfspeed();
        }

        if (isPressed("1a", gamepad1.a)) {
            zeroAng = robot.get180Yaw();
            if (zeroAng < 0) zeroAng += 360;
        }

        if (isPressed("1b", gamepad1.b)) field = !field;

        if (Math.abs(gamepad1.left_stick_y) > 0.1 ||
            Math.abs(gamepad1.left_stick_x) > 0.1 ||
            Math.abs(gamepad1.right_stick_x) > 0.1)
        {

            leftY = gamepad1.left_stick_y;
            leftX = gamepad1.left_stick_x;
            rightX = gamepad1.right_stick_x;

            if (!field) motorPower = robot.holonomicPower(leftX, leftY, rightX);
            else motorPower = robot.fieldOriented(leftX, leftY, rightX, zeroAng);

        } else {
            for (int i = 0; i < 4; i++) {
                motorPower[i] = 0;
            }
        }

        robot.setPowers(motorPower);

        //manual lift

        if (Math.abs(gamepad2.right_stick_y) > 0.1) {
            manip.manualLift(-gamepad2.right_stick_y);
        }
        else if (manual)
        {
            manip.stopLift();
        }

        // Auto lift
        //Lift High

        if (isPressed("2y", gamepad2.y))
        {
            manual = false;
            manip.automaticLift(3);
        }

        //Lift Mid
        if (isPressed("2b", gamepad2.b))
        {
            manual = false;
            manip.automaticLift(2);
        }

        //Lift Low
        if (isPressed("2a", gamepad2.a))
        {
            manual = false;
            manip.automaticLift(1);
        }


        if (isPressed("2x", gamepad2.x))
        {

            manip.automaticLift(0);
            waitTimer.reset();
            retract = true;
        }

        if (retract) {
            if (waitTimer.seconds() >= wait) manip.stopLift();
            if (waitTimer.seconds() >= wait + 1) {
                manip.resetArm();
                retract = false;
            }
        }

        /*
        retract = true;
        waitTimer.reset();
        manip.manualLift(-0.5);
        if (retract) {
            if (waitTimer.seconds() >= wait) manip.manualLift(0);
            if (waitTimer.seconds() >= wait + 1) {
                manip.resetArm();
                retract = false;
            }

        }*/

/*
        //Changes direction of carousel
        if (gamepad1.b)
        {
            duckDirection *= -1;
        }
*/
        //Blue Carousel
        if (gamepad1.right_bumper)
        {
            manip.blueCarousel();
        }
        //Red Carousel
        else if (gamepad1.left_bumper)
        {
            manip.redCarousel();
        }
        else
        {
            manip.carouselStop();
        }



        //Gate

        if (isPressed("lBumper", gamepad2.left_bumper)) manip.gate(true);

        if (isPressed("rBumper", gamepad2.right_bumper)) manip.gate(false);

        //Changes to outtake
        /*
        if (gamepad1.x)
        {
            intakeDirection += -1;
        }
        */

        //Intake
        if (Math.abs(gamepad2.left_trigger) > 0.1)
        {
            IT.setPower(gamepad2.left_trigger);
        }

        //Stop Intake
        else if (Math.abs(gamepad2.right_trigger) > 0.1)
        {
            IT.setPower(-gamepad2.right_trigger);
        }

        else
        {
            IT.setPower(0);
        }

        // Switch back to manual lift
        if (Math.abs(gamepad2.right_stick_y) > 0.1) manual = true;

        //telemetry.addData("R encoder", RL.getCurrentPosition());

        telemetry.addData("right stick y", gamepad2.right_stick_y);
        telemetry.addData("field", field);
        telemetry.addData("encoder", manip.RL.getCurrentPosition());
        telemetry.update();

    }


}
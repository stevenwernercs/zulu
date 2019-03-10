package com.trifidearth.zulu.malmo;

// --------------------------------------------------------------------------------------------------
//  Copyright (c) 2016 Microsoft Corporation
//  
//  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
//  associated documentation files (the "Software"), to deal in the Software without restriction,
//  including without limitation the rights to use, copy, modify, merge, publish, distribute,
//  sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//  
//  The above copyright notice and this permission notice shall be included in all copies or
//  substantial portions of the Software.
//  
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
//  NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
//  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
//  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// --------------------------------------------------------------------------------------------------

// To compile:  javac -cp MalmoJavaJar.jar JavaExamples_run_mission.java
// To run:      java -cp MalmoJavaJar.jar:. JavaExamples_run_mission  (on Linux)
//              java -cp MalmoJavaJar.jar;. JavaExamples_run_mission  (on Windows)

// To run from the jar file without compiling:   java -cp MalmoJavaJar.jar:JavaExamples_run_mission.jar -Djava.library.path=. JavaExamples_run_mission (on Linux)
//                                               java -cp MalmoJavaJar.jar;JavaExamples_run_mission.jar -Djava.library.path=. JavaExamples_run_mission (on Windows)

import com.microsoft.msr.malmo.*;
import com.trifidearth.zulu.utils.Utils;

import java.io.File;
import java.util.Map;

public class MinecraftANN
{
    static
    {
        System.loadLibrary("MalmoJava"); // attempts to load MalmoJava.dll (on Windows) or libMalmoJava.so (on Linux)
    }

    public static void main(String argv[]) throws Exception {
        AgentHost agent_host = new AgentHost();
        try
        {
            StringVector args = new StringVector();
            args.add("JavaExamples_run_mission");
            for( String arg : argv )
                args.add( arg );
            agent_host.parse( args );
        }
        catch( Exception e )
        {
            System.err.println( "ERROR: " + e.getMessage() );
            System.err.println( agent_host.getUsage() );
            System.exit(1);
        }
        if( agent_host.receivedArgument("help") )
        {
            System.out.println( agent_host.getUsage() );
            System.exit(0);
        }

        File missionXMLFile =  new File("/home/swerner/projects/zulu/malmo/levels/lvl1-Breakfast.xml");
        String missionXML = Utils.readFile(missionXMLFile);
        System.out.println(missionXML);

        //MissionSpec my_mission = new MissionSpec();
        MissionSpec my_mission = new MissionSpec(missionXML, true);
        my_mission.timeLimitInSeconds(100);
        my_mission.requestVideo( 640, 480 );
        my_mission.rewardForReachingPosition(19.5f,0.0f,19.5f,100.0f,1.1f);

        MissionRecordSpec my_mission_record = new MissionRecordSpec("./saved_data.tgz");
        my_mission_record.recordCommands();
        my_mission_record.recordMP4(20, 400000);
        my_mission_record.recordRewards();
        my_mission_record.recordObservations();

        System.out.println(my_mission.getAsXML(true));

        my_mission.allowAllAbsoluteMovementCommands();
        my_mission.allowAllChatCommands();
        my_mission.allowAllContinuousMovementCommands();
        my_mission.allowAllDiscreteMovementCommands();
        my_mission.allowAllInventoryCommands();

        System.out.println("Commands for role " + 0);
        StringVector commandHandlers = my_mission.getListOfCommandHandlers(0);
        for(int i = 0; i < commandHandlers.size(); i++) {
            String commandHandler = commandHandlers.get(i);
            StringVector allowedCommands = my_mission.getAllowedCommands(0, commandHandler);
            System.out.println("\tCommands for Handler " + commandHandler + ":");
            for(int j = 0; j < allowedCommands.size(); j++) {
                String command = allowedCommands.get(j);
                System.out.println("\t\t" + command);
            }
        }

        try {
            agent_host.startMission( my_mission, my_mission_record );
        }
        catch (MissionException e) {
            System.err.println( "Error starting mission: " + e.getMessage() );
            System.err.println( "Error code: " + e.getMissionErrorCode() );
            // We can use the code to do specific error handling, eg:
            if (e.getMissionErrorCode() == MissionException.MissionErrorCode.MISSION_INSUFFICIENT_CLIENTS_AVAILABLE)
            {
                // Caused by lack of available Minecraft clients.
                System.err.println( "Is there a Minecraft client running?");
            }
            System.exit(1);
        }

        WorldState world_state;

        int loadcounter=0;
        System.out.print( "Waiting for the mission to start" );
        do {
            if(loadcounter++ % 20 == 0) {
                System.out.print( "\r" );
            }
            System.out.print( "." );
            try {
                Thread.sleep(100);
            } catch(InterruptedException ex) {
                System.err.println( "User interrupted while waiting for mission to start." );
                return;
            }
            world_state = agent_host.getWorldState();
            for( int i = 0; i < world_state.getErrors().size(); i++ )
                System.err.println( "Error: " + world_state.getErrors().get(i).getText() );
        } while( !world_state.getIsMissionRunning() );
        System.out.println( "" );

        //Setup more level configs
        agent_host.sendCommand( "chat /effect @p minecraft:hunger 5 255");
        agent_host.sendCommand( "chat /effect @p minecraft:instant_damage 1 1");

        // main loop:
        do {
            agent_host.sendCommand( MalmoUtils.BOOLEAN_MOVEMENT_COMMAND.attack.get(false) );
            agent_host.sendCommand( MalmoUtils.BOOLEAN_MOVEMENT_COMMAND.use.get(false) );
            agent_host.sendCommand( MalmoUtils.BOOLEAN_MOVEMENT_COMMAND.jump.get(false) );
            agent_host.sendCommand( MalmoUtils.BOOLEAN_MOVEMENT_COMMAND.crouch.get(false) );
            agent_host.sendCommand( MalmoUtils.CONTINUOUS_MOVEMENT_COMMAND.pitch.get(0d) );
            agent_host.sendCommand( MalmoUtils.CONTINUOUS_MOVEMENT_COMMAND.turn.get(0d) );

            double choice = Math.random();
            if(choice < .03) {
                agent_host.sendCommand( MalmoUtils.BOOLEAN_MOVEMENT_COMMAND.crouch.get(true) );
            } else if(choice < .08) {
                agent_host.sendCommand( MalmoUtils.BOOLEAN_MOVEMENT_COMMAND.jump.get(true) );
            } else if(choice < .16) {
                agent_host.sendCommand(MalmoUtils.CONTINUOUS_MOVEMENT_COMMAND.strafe.get(MalmoUtils.getRandomRange()) );
            } else if(choice < .32) {
                if (Math.random() < .5) {
                    agent_host.sendCommand(MalmoUtils.BOOLEAN_MOVEMENT_COMMAND.attack.get(true));
                } else {
                    agent_host.sendCommand(MalmoUtils.BOOLEAN_MOVEMENT_COMMAND.use.get(true));
                }
            } else if(choice < .64) {
                agent_host.sendCommand( MalmoUtils.CONTINUOUS_MOVEMENT_COMMAND.move.get(MalmoUtils.getRandomRange()) );
            } else if(choice < .75) {
                agent_host.sendCommand(MalmoUtils.CONTINUOUS_MOVEMENT_COMMAND.pitch.get(MalmoUtils.getRandomRange()/2d));
            } else {
                agent_host.sendCommand( MalmoUtils.CONTINUOUS_MOVEMENT_COMMAND.turn.get(MalmoUtils.getRandomRange()) );
            }


            try {
                Thread.sleep(100);
            } catch(InterruptedException ex) {
                System.err.println( "User interrupted while mission was running." );
                return;
            }

            world_state = agent_host.getWorldState();
            TimestampedVideoFrameVector timestampedVideoFrameVector = world_state.getVideoFrames();
            System.out.println("Printing " + timestampedVideoFrameVector.size() + " VideoFrames:");
            for( int i = 0; i < timestampedVideoFrameVector.size(); i++ ) {
                TimestampedVideoFrame videoFrame = timestampedVideoFrameVector.get(i);
                ByteVector pixels = videoFrame.getPixels();
                System.out.println("\tVideoFrame " + i + " @ " + videoFrame.getHeight() + "x" + videoFrame.getWidth() + ": " + pixels);
            }

            TimestampedStringVector timestampedStringVector = world_state.getObservations();
            System.out.println("Printing " + timestampedStringVector.size() + " Observation sets:");
            for( int i = 0; i < timestampedStringVector.size(); i++ ) {
                TimestampedString observationTimestampedString = timestampedStringVector.get(i);
                String observationJson = observationTimestampedString.getText();
                System.out.println(observationJson);
                Map<MalmoUtils.OBSERVATION, String> observations = MalmoUtils.pasrseObservationJson(observationJson);
                System.out.println("\tObservation set " + i + " with " + observations.size() + " Observations:");
                for(Map.Entry<MalmoUtils.OBSERVATION,String> observation : observations.entrySet()) {
                    System.out.println("\t\tObservation set " + i + ": " + observation.getKey().name() + "\t: " + observation.getValue());
                }

            }

            System.out.print( "video,observations,rewards received: " );
            System.out.print( world_state.getNumberOfVideoFramesSinceLastState() + "," );
            System.out.print( world_state.getNumberOfObservationsSinceLastState() + "," );
            System.out.println( world_state.getNumberOfRewardsSinceLastState() );
            for( int i = 0; i < world_state.getRewards().size(); i++ ) {
                TimestampedReward reward = world_state.getRewards().get(i);
                System.out.println( "Summed reward: " + reward.getValue() );
            }
            for( int i = 0; i < world_state.getErrors().size(); i++ ) {
                TimestampedString error = world_state.getErrors().get(i);
                System.err.println( "Error: " + error.getText() );
            }
        } while( world_state.getIsMissionRunning() );

        System.out.println( "Mission has stopped." );
    }
}

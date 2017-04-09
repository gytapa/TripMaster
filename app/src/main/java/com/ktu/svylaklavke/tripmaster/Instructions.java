package com.ktu.svylaklavke.tripmaster;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by Gytis on 4/2/2017.
 */

public class Instructions {

    public String[] all_instructions;
    public int[] all_lengths;
    public LatLng[] all_latlngs;
    public String starting_point;
    public String destination_point;
    public int instruction_index;
    private int amount_of_instructions;

    public Instructions (String start, String destination) {
        all_instructions = new String[1000000];

        starting_point = start;
        destination_point = destination;
        instruction_index = 0;
        amount_of_instructions = 0;
    }

    public Instructions()
    {
        all_instructions = new String[1000000];
        all_lengths = new int[1000000];
        all_latlngs = new LatLng[1000000];
        instruction_index = 0;
        amount_of_instructions = 0;
    }


    public void add_instruction(String instruction, int meters, LatLng latlng){
        instruction = instruction.replace("<b>","");
        instruction = instruction.replace("</b>","");
        all_instructions[amount_of_instructions] = instruction;
        all_lengths[amount_of_instructions] = meters;
        all_latlngs[amount_of_instructions] = latlng;
        amount_of_instructions++;
    }

    public int get_meters()
    {
        return all_lengths[instruction_index];
    }
    public String get_instruction(){
            instruction_index++;
            return all_instructions[instruction_index-1];
    }

    public LatLng get_LatLng(){
        return all_latlngs[instruction_index];
    }

    public String all_instructions() {
        String message = "";
        for (int i = 0; i < amount_of_instructions; i++) {
            message = message + all_instructions[i];
        }
        return message;
    }
}


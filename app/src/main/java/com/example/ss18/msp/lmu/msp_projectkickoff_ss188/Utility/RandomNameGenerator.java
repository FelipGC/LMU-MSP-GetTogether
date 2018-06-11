package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public final class RandomNameGenerator {

    /** Generate a random Android agent codename */
    public static String generate(Context context) {

        try {
            String adjective = getRandomLineFromTheFile(context.getAssets().open("Adjectives.txt"));
            String noun = getRandomLineFromTheFile(context.getAssets().open("Nouns.txt"));
            return adjective + " " + noun;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "UnknownUser";
    }

    /**
     * Get random file line
     */
    private static String getRandomLineFromTheFile(InputStream file)
    {
        Scanner s = new Scanner(file);
        LinkedList list = new LinkedList();
        try {
            while (s.hasNextLine()) {
                list.add(s.nextLine());
            }
        } finally {
            s.close();
        }
        return (String) list.get(new Random().nextInt(list.size()));
    }
}

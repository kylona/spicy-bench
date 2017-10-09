/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hj.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class DataRaceDetector {

    public static void main(String args[]) {

        if (args.length == 0) {
            System.out.println("Usage: java DeadlockDetector <class_to_verify>");
            System.exit(1);
        }

        try {
            Class<?> y = Class.forName((args[0]) + "$Main");
            Class<?> string_array_class = args.getClass();
            Constructor<?> constructor = y.getConstructor(string_array_class);
            String[] new_args = new String[args.length - 1];

            for (int i = 0; i < args.length - 1; i++) {

                new_args[i] = args[i + 1];

            }

            java.lang.Object object = constructor.newInstance((java.lang.Object) new_args);

            Class<?> x = Class.forName("java.lang.Thread");
            Method method = x.getDeclaredMethod("start");
            method.invoke(object, (java.lang.Object[]) null);

        } catch (ClassNotFoundException | NoSuchMethodException |
                SecurityException | InstantiationException |
                IllegalAccessException | IllegalArgumentException |
                InvocationTargetException ex) {
            Logger.getLogger(DataRaceDetector.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}

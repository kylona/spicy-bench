/*
    Futures exploration and an attempt to get a total order on futures
*/
import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;

import static edu.rice.hj.Module1.*;
import static edu.rice.hj.Module2.isolated;

import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.HjFuture;

public class FuturesIso 
{

  public static void main(String[] args) throws SuspendableException 
  {
    launchHabaneroApp(new HjSuspendable() 
    {
        public void run() 
        {
            //Main Task / 1
            finish(new HjSuspendable()
            {
                int x = 3;
                //17 join (16 joins)
                final HjFuture<String> fut2 = future(() -> 
                {
                    x++;
                    //future 2
                    return "future 2";
                });
                //3
                String exec3 = new String("exec3");

                final HjFuture<String> fut4 = future(() -> 
                {
                    x--;
                    //future 4
                    return "future 4";
                });
                //5
                String exec5 = new String("exec5");
                public void run() 
                {

                    async(new HjRunnable() 
                    {
                        public void run() 
                        {
                            //async 6 .get() on 4
                            String async6 = new String(fut4.get() + "async6");
                            //7
                            String exec7 = new String("exec7");
                            finish(new HjSuspendable()
                            {
                                //15 join (8 joins)
                                public void run () 
                                {
                                    async(new HjRunnable()
                                    {
                                        // async 8
                                        public void run() 
                                        {
                                            String async8 = new String("async8");
                                        }
                                    });

                                    // 9 .get() on 2
                                    String exec9 = new String(fut2.get() + " exec9");
                                    //10
                                    String exec10 = new String("exec10");
                                    
                                    finish(new HjSuspendable()
                                    {   
                                        //14 join (13 joins)
                                        public void run() 
                                        {
                                            async(new HjRunnable()
                                            {
                                                //async 11
                                                public void run() 
                                                {
                                                    String async11 = new String("async11");
                                                    finish(new HjSuspendable()
                                                    {
                                                        //13 join (12 joins)
                                                        public void run() 
                                                        {
                                                            async(new HjRunnable() 
                                                            {
                                                                public void run()
                                                                {
                                                                    String async12 = new String("async12");
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
                //16
                String exec16 = new String("exec16");
            });
        }
    });
  }
}

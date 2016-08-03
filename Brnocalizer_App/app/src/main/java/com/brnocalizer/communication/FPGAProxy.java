/**
 * @class FPGAProxy.java
 *
 * @brief Fais le lien entre l'IHM android et la partie communication à la classe PreteurSurGage
 *
 * @details Permet a l'IHM android de demander la liste du materiel disponible et de créer les
 * emprunts quand l'utilisateur arrive au bout de sa démarche.
 *
 * Cette classe permet aussi à l'IHM de demander la liste du matériel emprunté par un certain
 * emprunteur.
 *
 *
 * @version 1.0
 * @date 27/04/2016
 * @author Thomas Fardeau
 * @copyright (c) BSD 2 clauses, <2016>, <A1, ESEO>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 *
 *
 */


package com.brnocalizer.communication;


public class FPGAProxy
{


    /**
     * setter of FPGAProxy
     */
    private FPGAProxy()
    {

    }

    /**
     * creating a singleton of FPGA proxy
     */
    private static class FPGAProxyHolder
    {
        private final static FPGAProxy instance = new FPGAProxy();
    }

    /**
     * way to acces FPGA proxy's singleton
     * @return FPGAProxy's instance
     */
    public static FPGAProxy getInstance()
    {
        return FPGAProxyHolder.instance;
    }


    /**
     * asking postman to send accelerometer data
     *
     * @param data : List of accelerometer data gathered previously
     */
    public void sendAccelerometerData(String data)
    {
        Postman.getInstance().sendMessage(Protocol.ACCELEROMETER_ID+Protocol.SEPARATOR, data);
    }

    /**
     * asking postman to send magnetic field data
     *
     * @param data : List of magnetic field data gathered previously
     */
    public void sendGyroscopeData(String data) {
        Postman.getInstance().sendMessage(Protocol.GYROSCOPE_ID+Protocol.SEPARATOR, data);
    }

    /**
     * asking postman to send compass data
     *
     * @param data : List of compass data gathered previously
     */
    public void sendCompassData(String data)
    {
        Postman.getInstance().sendMessage(Protocol.COMPASS_ID+Protocol.SEPARATOR, data);
    }


    /**
     * asking postman to send magnetic field data
     *
     * @param data : List of magnetic field data gathered previously
     */
    public void sendGPSData(String data)
    {
        Postman.getInstance().sendMessage(Protocol.GPS_ID+Protocol.SEPARATOR, data);
    }


    /**
     * asking Postman to send the stop message to the server in order to begin sending the video data
     */
    public void sendStop(){
        Postman.getInstance().sendMessage(Protocol.STOP_ID, Protocol.SEPARATOR);
    }



    public Postman getPostman()
    {
        return Postman.getInstance();
    }
}

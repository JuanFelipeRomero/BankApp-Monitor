package org.ucentral;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Monitor extends UnicastRemoteObject implements MonitorInterface {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isServidorActivo() throws RemoteException {
        return ServerConnection.getInstance().isServidorActivo();
    }

    protected Monitor() throws RemoteException {
        super();
    }

}

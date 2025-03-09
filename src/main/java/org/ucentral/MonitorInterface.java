package org.ucentral;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MonitorInterface extends Remote {
    boolean isServidorActivo() throws RemoteException;
}

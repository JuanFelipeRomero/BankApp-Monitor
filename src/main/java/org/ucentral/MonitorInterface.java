package org.ucentral;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MonitorInterface extends Remote {
    boolean isServidorActivo() throws RemoteException;
    List<ServidorInfo> obtenerEstadoServidores() throws RemoteException; // Nuevo método
}

package org.ucentral;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.stream.Collectors;

public class Monitor extends UnicastRemoteObject implements MonitorInterface {

    private static final long serialVersionUID = 1L;

    protected Monitor() throws RemoteException {
        super();
    }

    @Override
    public boolean isServidorActivo() throws RemoteException {
        return ServerConnection.getInstance().isServidorActivo();
    }

    @Override
    public List<ServidorInfo> obtenerEstadoServidores() throws RemoteException {
        return ConfigLoader.getServidores().stream()
                .map(s -> new ServidorInfo(s.getIp(), s.getPuerto(), ServerConnection.getInstance().isServidorActivo(s)))
                .collect(Collectors.toList());
    }
}


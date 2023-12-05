package edu.oswego.cs;

import java.util.List;

public class PortPool {

    List<Integer> ports;
    int currentPortIndex;

    public PortPool(List<Integer> ports) {
        this.ports = ports;
        currentPortIndex = 0;
    }

    synchronized public Integer getNextPort() {
        Integer port = ports.get(currentPortIndex);
        currentPortIndex++;
        // start at the beginning of the list again
        if (currentPortIndex >= ports.size()) currentPortIndex = 0;
        return port;
    }
}

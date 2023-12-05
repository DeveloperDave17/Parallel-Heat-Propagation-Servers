package edu.oswego.cs;

public class CalculationProtocol {

    private ProtocolState state = ProtocolState.WAITING;

    public String processInput(String clientInput) {
        if (state == ProtocolState.WAITING) {
            state = ProtocolState.PROCESSING;
        } else if (clientInput.equals("Done")) {
            state = ProtocolState.DONE;
        }
        return clientInput;
    }
}

package net.nandgr.debugger.cfg.graphviz;

import net.nandgr.debugger.Main;
import net.nandgr.debugger.cfg.beans.BytecodeChunk;
import net.nandgr.debugger.cfg.beans.OpcodeSource;
import net.nandgr.debugger.node.response.json.DebugTraceTransactionLog;
import java.util.Map;

// Not the most elegant way to create the graph, but it works for now
public class GraphVizCreator {

    private final Map<Integer, BytecodeChunk> chunks;
    private final Map<Integer, DebugTraceTransactionLog> trace;
    private final String contractName;

    public GraphVizCreator(Map<Integer, BytecodeChunk> chunks, Map<Integer, DebugTraceTransactionLog> trace, String contractName) {
        this.chunks = chunks;
        this.trace = trace;
        this.contractName = contractName;
    }

    public String buildStringGraph() {
        StringBuilder sb = new StringBuilder("digraph \" \" {" + System.lineSeparator())
        .append("graph [splines=ortho ranksep=\"2\" nodesep=\"2\" bgcolor=\"#4A4A4A\"]").append(System.lineSeparator())
        .append("rankdir=LR").append(System.lineSeparator())
        .append("node [shape=plain fillcolor=\"#2A2A2A\" style=filled fontcolor=\"#12cc12\" fontname=\"Courier\"]").append(System.lineSeparator());
        for (BytecodeChunk bytecodeChunk : chunks.values()) {
            String coloredNode = "";
            OpcodeSource firstOpcode = bytecodeChunk.getOpcodes().get(0);
            if (Main.arguments.onlyTraceOpcodes && !trace.containsKey(firstOpcode.getOffset())) {
                continue;
            }
            if (trace.containsKey(firstOpcode.getOffset())) {
                coloredNode = " fontcolor=\"red\" ";
            }
            sb.append(bytecodeChunk.getId()).append("[").append(coloredNode).append("label=").append(buildLabel(bytecodeChunk)).append("]").append(System.lineSeparator());

            if (checkIfAppendBranch(bytecodeChunk.getBranchA())) {
                sb.append(bytecodeChunk.getId()).append("->").append(bytecodeChunk.getBranchA().getId()).append("[color=\"#12cc12\"];").append(System.lineSeparator());
            }
            if (checkIfAppendBranch(bytecodeChunk.getBranchB())) {
                sb.append(bytecodeChunk.getId()).append("->").append(bytecodeChunk.getBranchB().getId()).append("[color=\"#12cc12\"];").append(System.lineSeparator());
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private boolean checkIfAppendBranch(BytecodeChunk branch) {
        if (branch == null) {
            return false;
        }
        if (!Main.arguments.onlyTraceOpcodes) {
            return true;
        }
        OpcodeSource firstOpcode = branch.getOpcodes().get(0);
        return trace.containsKey(firstOpcode.getOffset());
    }

    private String buildLabel(BytecodeChunk bytecodeChunk) {
        StringBuilder sb= new StringBuilder("< <TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">").append(System.lineSeparator());
        for (OpcodeSource opcodeSource : bytecodeChunk.getOpcodes()) {
            String id = opcodeSource.getOffset() + "#" + opcodeSource.getBegin() + "#" + opcodeSource.getEnd();
            sb.append("<TR><TD ID=\"").append(id).append("#offset#").append(contractName).append("\" HREF=\" \">0x")
            .append(String.format("%04X", opcodeSource.getOffset()))
            .append("</TD><TD ID=\"").append(id).append("#instr#").append(contractName).append("\" HREF=\" \">")
            .append(opcodeSource.getOpcode())
            .append("</TD>");
            if (opcodeSource.getParameter() != null) {
                sb.append("<TD>0x").append(opcodeSource.getParameter().toString(16)).append("</TD>");
            }
            sb.append("</TR>").append(System.lineSeparator());
        }
        sb.append("</TABLE> >").append(System.lineSeparator());
        return sb.toString();
    }
}

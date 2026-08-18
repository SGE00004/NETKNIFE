package com.netknife.tools.cryptojackingdetector.monitor;

import com.netknife.common.util.SystemCommandRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Averigua que procesos tienen una ventana visible en pantalla, via PowerShell
 * (Get-Process). Un proceso de mineria oculto tipicamente NO tiene ventana: es una
 * senal adicional (no definitiva por si sola) de que un uso alto de CPU es
 * sospechoso en vez de una app que el usuario esta usando activamente.
 */
@Component
public class WindowsVisibleWindowChecker {

    private static final String[] COMMAND = {
            "powershell", "-NoProfile", "-NonInteractive", "-Command",
            "Get-Process | Where-Object { $_.MainWindowTitle -ne '' } | Select-Object -ExpandProperty Id"
    };

    private static final Pattern PID_LINE = Pattern.compile("^\\s*(\\d+)\\s*$");

    private final SystemCommandRunner commandRunner;

    public WindowsVisibleWindowChecker(SystemCommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public Set<Integer> pidsWithVisibleWindow() {
        return commandRunner.run(COMMAND).map(WindowsVisibleWindowChecker::parse).orElse(Set.of());
    }

    static Set<Integer> parse(String output) {
        Set<Integer> pids = new HashSet<>();
        for (String line : output.split("\\R")) {
            Matcher matcher = PID_LINE.matcher(line);
            if (matcher.matches()) {
                pids.add(Integer.parseInt(matcher.group(1)));
            }
        }
        return pids;
    }
}

package com.sentinel.ac.check;

import com.sentinel.ac.check.impl.AutoClickerA;
import com.sentinel.ac.check.impl.AutoClickerB;
import com.sentinel.ac.check.impl.BadPacketsA;
import com.sentinel.ac.check.impl.FastBreakA;
import com.sentinel.ac.check.impl.FlyA;
import com.sentinel.ac.check.impl.FlyB;
import com.sentinel.ac.check.impl.KillAuraA;
import com.sentinel.ac.check.impl.KillAuraB;
import com.sentinel.ac.check.impl.KillAuraC;
import com.sentinel.ac.check.impl.NoFallA;
import com.sentinel.ac.check.impl.NukerA;
import com.sentinel.ac.check.impl.NukerB;
import com.sentinel.ac.check.impl.NukerC;
import com.sentinel.ac.check.impl.ReachA;
import com.sentinel.ac.check.impl.SpeedA;
import com.sentinel.ac.check.impl.TimerA;
import com.sentinel.ac.data.PlayerData;

import java.util.ArrayList;
import java.util.List;

/** Lista de todos os checks. Para criar um novo, adicione-o aqui. */
public final class CheckRegistry {

    private CheckRegistry() {
    }

    public static List<Check> createFor(PlayerData data) {
        List<Check> list = new ArrayList<>();
        // Movimento
        list.add(new SpeedA(data));
        list.add(new FlyA(data));
        list.add(new FlyB(data));
        list.add(new NoFallA(data));
        // Combate
        list.add(new ReachA(data));
        list.add(new KillAuraA(data));
        list.add(new KillAuraB(data));
        list.add(new KillAuraC(data));
        list.add(new AutoClickerA(data));
        list.add(new AutoClickerB(data));
        // Pacotes
        list.add(new BadPacketsA(data));
        list.add(new TimerA(data));
        // Mineração
        list.add(new FastBreakA(data));
        list.add(new NukerA(data));
        list.add(new NukerB(data));
        list.add(new NukerC(data));
        return list;
    }
}

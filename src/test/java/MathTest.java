import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;

public class MathTest {

    @Test
    public void doubleMath() {
        System.out.println(1000L * 1d / 2154L);
        System.out.println(1000L / 2154L * 1.0d);
    }

    @Test
    public void trigger() {
        PeriodicTrigger periodicTrigger = new PeriodicTrigger(Duration.ofMillis(300));
        periodicTrigger.setFixedRate(true);
        for (int i = 0; i < 10; i++) {
//            System.out.println(periodicTrigger.nextExecution());
        }
    }

}

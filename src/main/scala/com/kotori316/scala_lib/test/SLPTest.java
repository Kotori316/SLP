package com.kotori316.scala_lib.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

@Mod(SLPTest.modID)
class SLPTest {
    public static final String modID = "slp_test";
    private static final Logger LOGGER = LogManager.getLogger("SLPTest");

    public SLPTest() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::dataEvent);
    }

    public void dataEvent(GatherDataEvent event) {
        if (event.includeDev())
            event.getGenerator().addProvider(new TestProvider());
    }

    private static class TestProvider implements IDataProvider {

        private static boolean isInCI() {
            return Boolean.parseBoolean(System.getenv("GITHUB_ACTIONS"));
        }

        @Override
        public void act(DirectoryCache cache) {
            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                    selectPackage(SLPTest.class.getPackage().getName())
                )
                .build();

            if (isInCI()) {
                try {
                    Files.createFile(Paths.get("..", "test_started.txt"));
                } catch (IOException e) {
                    LOGGER.error("File IO", e);
                }
            }

            Launcher launcher = LauncherFactory.create();

            // Register a listener of your choice
            SummaryGeneratingListener listener = new SummaryGeneratingListener();
            launcher.registerTestExecutionListeners(listener);

            launcher.execute(request);

            TestExecutionSummary summary = listener.getSummary();
            // Do something with the TestExecutionSummary.
            StringWriter stream = new StringWriter();
            summary.printTo(new PrintWriter(stream));
            LOGGER.info(stream.toString());
            List<Throwable> errors = summary.getFailures().stream()
                .map(TestExecutionSummary.Failure::getException).collect(Collectors.toList());
            errors.forEach(t -> LOGGER.fatal("Test failed.", t));
            if (isInCI() && !errors.isEmpty()) {
                try (BufferedWriter w = Files.newBufferedWriter(Paths.get("..", "error-trace.txt"));
                     PrintWriter writer = new PrintWriter(w)) {
                    errors.forEach(t -> t.printStackTrace(writer));
                } catch (IOException e) {
                    LOGGER.error("File IO", e);
                }
            }
        }

        @Override
        public String getName() {
            return "TestProvider of SLP";
        }
    }
}

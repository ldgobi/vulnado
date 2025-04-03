package com.scalesec.vulnado;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VulnadoApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void Cowsay_Run_ShouldReturnExpectedOutput() throws IOException {
        // Mocking the Process and BufferedReader
        Process mockProcess = Mockito.mock(Process.class);
        String expectedOutput = "Mocked Cowsay Output\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(expectedOutput.getBytes());
        Mockito.when(mockProcess.getInputStream()).thenReturn(inputStream);

        ProcessBuilder mockProcessBuilder = Mockito.mock(ProcessBuilder.class);
        Mockito.when(mockProcessBuilder.start()).thenReturn(mockProcess);

        // Injecting the mocked ProcessBuilder into the Cowsay class
        Cowsay cowsay = new Cowsay() {
            @Override
            protected ProcessBuilder createProcessBuilder() {
                return mockProcessBuilder;
            }
        };

        String input = "Hello, World!";
        String actualOutput = cowsay.run(input);

        // Asserting the output
        assertEquals("The output should match the mocked output", expectedOutput, actualOutput);
    }

    @Test
    public void Cowsay_Run_ShouldHandleExceptionGracefully() {
        // Mocking the ProcessBuilder to throw an exception
        ProcessBuilder mockProcessBuilder = Mockito.mock(ProcessBuilder.class);
        Mockito.when(mockProcessBuilder.start()).thenThrow(new IOException("Mocked Exception"));

        // Injecting the mocked ProcessBuilder into the Cowsay class
        Cowsay cowsay = new Cowsay() {
            @Override
            protected ProcessBuilder createProcessBuilder() {
                return mockProcessBuilder;
            }
        };

        String input = "Hello, World!";
        String actualOutput = cowsay.run(input);

        // Asserting the output
        assertTrue("The output should be empty when an exception occurs", actualOutput.isEmpty());
    }
}

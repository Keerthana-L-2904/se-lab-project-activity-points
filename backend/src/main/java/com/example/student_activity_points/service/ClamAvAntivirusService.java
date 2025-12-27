package com.example.student_activity_points.service;

import com.example.student_activity_points.util.ExcelFileValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * ClamAV antivirus service implementation
 * This is a basic implementation that connects to ClamAV daemon
 * 
 * To use this, you need to have ClamAV installed and running:
 * - Linux: sudo apt-get install clamav clamav-daemon
 * - Mac: brew install clamav
 * - Windows: Download from https://www.clamav.net/downloads
 * 
 * Start the daemon: sudo systemctl start clamav-daemon
 */
@Service
public class ClamAvAntivirusService implements ExcelFileValidationUtil.AntivirusService {

    private static final Logger log = LoggerFactory.getLogger(ClamAvAntivirusService.class);

    @Value("${antivirus.enabled:false}")
    private boolean antivirusEnabled;

    @Value("${antivirus.clamav.host:localhost}")
    private String clamavHost;

    @Value("${antivirus.clamav.port:3310}")
    private int clamavPort;

    @Value("${antivirus.timeout:30000}")
    private int timeout;

    private static final int CHUNK_SIZE = 2048;

    @Override
    public boolean isFileSafe(MultipartFile file) throws IOException {
        // If antivirus is disabled, return true (consider file safe)
        if (!antivirusEnabled) {
            log.debug("Antivirus scanning is disabled");
            return true;
        }

        try {
            return scanFile(file.getInputStream());
        } catch (IOException e) {
            log.error("Error during virus scan for file: {}", file.getOriginalFilename(), e);
            // Fail secure: if scan fails, reject the file
            return false;
        }
    }

    /**
     * Scans file using ClamAV INSTREAM command
     */
    private boolean scanFile(InputStream inputStream) throws IOException {
        try (Socket socket = new Socket(clamavHost, clamavPort)) {
            socket.setSoTimeout(timeout);

            // Send INSTREAM command
            OutputStream outStream = socket.getOutputStream();
            outStream.write("zINSTREAM\0".getBytes(StandardCharsets.UTF_8));
            outStream.flush();

            // Send file in chunks
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // Send chunk size (4 bytes, network byte order)
                outStream.write(ByteBuffer.allocate(4).putInt(bytesRead).array());
                // Send chunk data
                outStream.write(buffer, 0, bytesRead);
                outStream.flush();
            }

            // Send zero-length chunk to indicate end of stream
            outStream.write(new byte[]{0, 0, 0, 0});
            outStream.flush();

            // Read response
            byte[] response = new byte[1024];
            int responseLength = socket.getInputStream().read(response);
            
            if (responseLength > 0) {
                String result = new String(response, 0, responseLength, StandardCharsets.UTF_8).trim();
                log.debug("ClamAV scan result: {}", result);

                // ClamAV returns "stream: OK" if file is clean
                // ClamAV returns "stream: <virus-name> FOUND" if virus detected
                if (result.contains("OK")) {
                    return true;
                } else if (result.contains("FOUND")) {
                    log.warn("Virus detected: {}", result);
                    return false;
                } else {
                    log.error("Unexpected ClamAV response: {}", result);
                    // Fail secure
                    return false;
                }
            }

            log.error("No response received from ClamAV");
            return false;

        } catch (IOException e) {
            log.error("Failed to connect to ClamAV daemon at {}:{}", clamavHost, clamavPort, e);
            throw e;
        }
    }

    /**
     * Simple ping to check if ClamAV is running
     */
    public boolean isClamAvAvailable() {
        if (!antivirusEnabled) {
            return false;
        }

        try (Socket socket = new Socket(clamavHost, clamavPort)) {
            socket.setSoTimeout(5000);
            OutputStream outStream = socket.getOutputStream();
            
            // Send PING command
            outStream.write("zPING\0".getBytes(StandardCharsets.UTF_8));
            outStream.flush();

            // Read response
            byte[] response = new byte[256];
            int length = socket.getInputStream().read(response);
            
            if (length > 0) {
                String result = new String(response, 0, length, StandardCharsets.UTF_8).trim();
                return "PONG".equals(result);
            }
            
            return false;
        } catch (IOException e) {
            log.warn("ClamAV is not available at {}:{}", clamavHost, clamavPort);
            return false;
        }
    }
}
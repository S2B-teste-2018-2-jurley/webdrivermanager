/*
 * (C) Copyright 2015 Boni Garcia (http://bonigarcia.github.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.github.bonigarcia.wdm;

import static io.github.bonigarcia.wdm.Config.isNullOrEmpty;
import static io.github.bonigarcia.wdm.DriverManagerType.OPERA;
import static io.github.bonigarcia.wdm.Shell.getVersionFromPosixOutput;
import static io.github.bonigarcia.wdm.Shell.runAndWait;
import static java.util.Optional.empty;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Manager for Opera.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class OperaDriverManager extends WebDriverManager {

    protected OperaDriverManager() {
        driverManagerType = OPERA;
        exportParameterKey = "wdm.operaDriverExport";
        driverVersionKey = "wdm.operaDriverVersion";
        driverUrlKey = "wdm.operaDriverUrl";
        driverMirrorUrlKey = "wdm.operaDriverMirrorUrl";
        driverName = "operadriver";
    }

    @Override
    protected List<URL> getDrivers() throws IOException {
        return getDriversFromGitHub();
    }

    @Override
    protected String getCurrentVersion(URL url, String driverName) {
        if (isUsingTaobaoMirror()) {
            int i = url.getFile().lastIndexOf(SLASH);
            int j = url.getFile().substring(0, i).lastIndexOf(SLASH) + 1;
            return url.getFile().substring(j, i);
        } else {
            return url.getFile().substring(
                    url.getFile().indexOf(SLASH + "v") + 2,
                    url.getFile().lastIndexOf(SLASH));
        }
    }

    @Override
    protected File postDownload(File archive) {
        log.trace("Post processing for Opera: {}", archive);

        File extractFolder = archive.getParentFile()
                .listFiles(getFolderFilter())[0];
        if (!extractFolder.isFile()) {
            File target;
            try {
                log.trace("Opera extract folder (to be deleted): {}",
                        extractFolder);
                File[] listFiles = extractFolder.listFiles();
                int i = 0;
                File operadriver;
                boolean isOperaDriver;
                do {
                    if (i >= listFiles.length) {
                        throw new WebDriverManagerException(
                                "Driver binary for Opera not found in zip file");
                    }
                    operadriver = listFiles[i];
                    isOperaDriver = config().isExecutable(operadriver)
                            && operadriver.getName().contains(getDriverName());
                    i++;
                    log.trace("{} is valid: {}", operadriver, isOperaDriver);
                } while (!isOperaDriver);
                log.info("Operadriver binary: {}", operadriver);

                target = new File(archive.getParentFile().getAbsolutePath(),
                        operadriver.getName());
                log.trace("Operadriver target: {}", target);

                downloader.renameFile(operadriver, target);
            } finally {
                downloader.deleteFolder(extractFolder);
            }
            return target;
        } else {
            return super.postDownload(archive);
        }
    }

    @Override
    protected Optional<String> getBrowserVersion() {
        if (IS_OS_WINDOWS) {
            log.warn("Not implemented yet");
        } else if (IS_OS_LINUX) {
            String browserVersionOutput = runAndWait("opera", "--version");
            if (!isNullOrEmpty(browserVersionOutput)) {
                return Optional.of(
                        getVersionFromPosixOutput(browserVersionOutput, ""));
            }
        } else if (IS_OS_MAC) {
            String browserVersionOutput = runAndWait(
                    "/Applications/Opera.app/Contents/MacOS/Opera",
                    "--version");
            if (!isNullOrEmpty(browserVersionOutput)) {
                return Optional.of(
                        getVersionFromPosixOutput(browserVersionOutput, ""));
            }
        }
        return empty();
    }

}

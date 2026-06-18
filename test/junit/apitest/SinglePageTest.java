/*
 * Copyright (c) 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package apitest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import apitest.lib.APITester;
import toolbox.JavadocTask;
import toolbox.ModuleBuilder;
import toolbox.Task;

/**
 * Tests for the single page report feature.
 */
public class SinglePageTest extends APITester {
    /**
     * Tests "all-changes.html" page containing all changes for a package
     * in a single file.
     *
     * @throws IOException
     */
    @Test
    public void testPackageAllChangesPage() throws IOException {
        Path base = getScratchDir();
        log.println(base);

        List<String> options = new ArrayList<>();

        int APIS = 2;
        for (int a = 0; a < APIS; a++) {
            String apiName = "api" + a;
            Path apiDir = base.resolve(apiName).resolve("src");

            ModuleBuilder mb = new ModuleBuilder(tb, "m")
                    .exports("p")
                    .exports("q")
                    .classes("package p; public class Changed { public "
                                    + (a == 0 ? "int" : "long") + " n; }\n",
                            "package p; public class Same { public int f; }\n",
                            "package q; public class Unchanged { }\n");
            if (a == 0) {
                mb.classes("package p; public class Removed { }\n");
            } else {
                mb.classes("package p; public class Added { }\n");
            }
            mb.write(apiDir);

            options.addAll(List.of(
                    "--api", apiName,
                    "--module-source-path", apiDir.toString()));
        }

        Path outDir = base.resolve("out");
        options.addAll(List.of(
                "--include", "m/**",
                "-d", outDir.toString()));

        log.println("Options: " + options);

        run(options);

        Path allChanges = outDir.resolve("m").resolve("p").resolve("all-changes.html");
        Assertions.assertTrue(Files.exists(allChanges), "all-changes.html not found");
        checkOutput(allChanges, "Changed", "Added", "Removed");
        checkOutput(allChanges, "package-summary.html", "View changes in multiple pages");

        Path packageSummary = outDir.resolve("m").resolve("p").resolve("package-summary.html");
        checkOutput(packageSummary, "all-changes.html", "View changes in one page");

        Path allChangesUnchanged = outDir.resolve("m").resolve("q").resolve("all-changes.html");
        Assertions.assertTrue(Files.exists(allChangesUnchanged),
                "all-changes.html not found in unchanged package");
    }
}

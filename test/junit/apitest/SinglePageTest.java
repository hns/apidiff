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

        checkFile(outDir.resolve("index.html"), false);
        checkFile(outDir.resolve("all-changes.html"), true);
        checkFile(outDir.resolve("m").resolve("module-summary.html"), false);
        checkFile(outDir.resolve("m").resolve("all-changes.html"), true);
        checkFile(outDir.resolve("m").resolve("p").resolve("package-summary.html"), false);
        checkFile(outDir.resolve("m").resolve("p").resolve("all-changes.html"), true);

        Path allChangesUnchanged = outDir.resolve("m").resolve("q").resolve("all-changes.html");
        Assertions.assertTrue(Files.exists(allChangesUnchanged),
                "all-changes.html not found in unchanged package");
    }

    private void checkFile(Path file, boolean isSinglePage) throws IOException {
        Assertions.assertTrue(Files.exists(file), "File " + file + " not found");

        if (isSinglePage) {
            checkOutput(file, "Overview", "View changes in multiple pages");
            checkOutput(file, """
                    <section class="changed-element" id="id-m/p"><h2><a href="#id-m/p">Package p</a></h2>
                    <div class="changed-type-content"><div class="element"><span class="diff">&ne;</span><div class="signature"><span class="keyword">package</span> p</div>
                    </div>
                    <section class="enclosed"><h3>Types</h3>
                    <ul><li><span><span class="add">&plus;</span> <a href="#id-m/p.Added">Added</a></span></li><li><span><span class="diff">&ne;</span> <a href="#id-m/p.Changed">Changed</a></span></li><li><span><span class="remove">&minus;</span> <a href="#id-m/p.Removed">Removed</a></span></li><li class="unchanged"><span><span class="same">&equals;</span> <a href="#id-m/p.Same">Same</a></span></li></ul>
                    </section>
                    </div>
                    </section>
                    <section class="changed-element" id="id-m/p.Added"><h2><a href="#id-m/p.Added">Class Added</a></h2>
                    <div class="changed-type-content"><div class="element"><span class="add">&plus;</span><span class="missing missing-caption-add">Only in: api1; not in: api0.</span><div class="signature"><span class="modifiers"><span class="keyword">public</span></span> <span class="keyword">class</span> Added <div class="superclass"><span class="keyword">extends</span> java.lang.Object</div>
                    </div>
                    </div>
                    <section class="enclosed"><h3>Constructors</h3>
                    <ul><li><div class="element" id="&lt;init&gt;()"><span class="add">&plus;</span><div class="signature"><span class="modifiers"><span class="keyword">public</span></span> Added()</div>
                    </div>
                    </li></ul>
                    </section>
                    </div>
                    </section>
                    <section class="changed-element" id="id-m/p.Changed"><h2><a href="#id-m/p.Changed">Class Changed</a></h2>
                    <div class="changed-type-content"><div class="element"><span class="diff">&ne;</span><div class="signature"><span class="modifiers"><span class="keyword">public</span></span> <span class="keyword">class</span> Changed <div class="superclass"><span class="keyword">extends</span> java.lang.Object</div>
                    </div>
                    </div>
                    <section class="enclosed"><h3>Fields</h3>
                    <ul><li><div class="element" id="n"><span class="diff">&ne;</span><div class="signature"><span class="modifiers"><span class="keyword">public</span></span> <span class="diffs"><span class="api" title="api0"><span class="keyword">int</span></span><span class="api" title="api1"><span class="keyword">long</span></span></span> n</div>
                    </div>
                    </li></ul>
                    </section>
                    <div class="unchanged"><section class="enclosed"><h3>Constructors</h3>
                    <ul><li><div class="element" id="&lt;init&gt;()"><span class="same">&equals;</span><div class="signature"><span class="modifiers"><span class="keyword">public</span></span> Changed()</div>
                    </div>
                    </li></ul>
                    </section>
                    </div>
                    </div>
                    </section>
                    <section class="changed-element" id="id-m/p.Removed"><h2><a href="#id-m/p.Removed">Class Removed</a></h2>
                    <div class="changed-type-content"><div class="element"><span class="remove">&minus;</span><span class="missing missing-caption-remove">Only in: api0; not in: api1.</span><div class="signature"><span class="modifiers"><span class="keyword">public</span></span> <span class="keyword">class</span> Removed <div class="superclass"><span class="keyword">extends</span> java.lang.Object</div>
                    </div>
                    </div>
                    <section class="enclosed"><h3>Constructors</h3>
                    <ul><li><div class="element" id="&lt;init&gt;()"><span class="remove">&minus;</span><div class="signature"><span class="modifiers"><span class="keyword">public</span></span> Removed()</div>
                    </div>
                    </li></ul>
                    </section>
                    </div>
                    </section>""");
        } else {
            checkOutput(file, "Overview", "all-changes.html", "View changes in one page");
        }
    }
}

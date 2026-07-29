/*
 * Copyright (c) 2019, 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package jdk.codetools.apidiff.report.html;

import java.io.File;

import jdk.codetools.apidiff.model.ElementKey;
import jdk.codetools.apidiff.model.ElementKey.ExecutableElementKey;
import jdk.codetools.apidiff.model.ElementKey.ModuleElementKey;
import jdk.codetools.apidiff.model.ElementKey.PackageElementKey;
import jdk.codetools.apidiff.model.ElementKey.TypeElementKey;
import jdk.codetools.apidiff.model.ElementKey.TypeParameterElementKey;
import jdk.codetools.apidiff.model.ElementKey.VariableElementKey;

class GetFileVisitor implements ElementKey.Visitor<DocPath, Boolean> {

    DocPath getFile(ElementKey k, Boolean singlePageMode) {
        return k.accept(this, singlePageMode);
    }

    @Override
    public DocPath visitModuleElement(ModuleElementKey k, Boolean singlePageMode) {
        DocPath path = singlePageMode
                ? PageReporter.ALL_CHANGES
                : DocPath.create("module-summary.html");
        return getModuleDir(k).resolve(path);
    }

    private DocPath getModuleDir(ModuleElementKey k) {
        return (k == null)
                ? DocPath.empty
                : DocPath.create(k.name.toString());
    }

    @Override
    public DocPath visitPackageElement(PackageElementKey k, Boolean singlePageMode) {
        DocPath path = singlePageMode
                ? PageReporter.ALL_CHANGES
                : DocPath.create("package-summary.html");
        return getPackageDir(k).resolve(path);
    }

    private DocPath getPackageDir(PackageElementKey k) {
        return (k == null)
                ? DocPath.empty
                : getModuleDir((ModuleElementKey) k.moduleKey)
                        .resolve(k.name.toString().replace(".", File.separator));
    }

    @Override
    public DocPath visitTypeElement(TypeElementKey k, Boolean singlePageMode) {
        StringBuilder fn = new StringBuilder(k.name + ".html");
        while (k.enclosingKey instanceof TypeElementKey) {
            k = (TypeElementKey) k.enclosingKey;
            fn.insert(0,k.name + ".");
        }
        return getPackageDir((PackageElementKey) k.enclosingKey).resolve(fn.toString());
    }

    @Override
    public DocPath visitExecutableElement(ExecutableElementKey k, Boolean singlePageMode) {
        return k.typeKey.accept(this, singlePageMode);
    }

    @Override
    public DocPath visitVariableElement(VariableElementKey k, Boolean singlePageMode) {
        return k.typeKey.accept(this, singlePageMode);
    }

    @Override
    public DocPath visitTypeParameterElement(TypeParameterElementKey k, Boolean singlePageMode) {
        return null;
    }
}

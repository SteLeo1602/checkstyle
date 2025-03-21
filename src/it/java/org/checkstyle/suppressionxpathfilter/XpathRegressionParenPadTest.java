///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2025 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
///////////////////////////////////////////////////////////////////////////////////////////////

package org.checkstyle.suppressionxpathfilter;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.checks.whitespace.AbstractParenPadCheck;
import com.puppycrawl.tools.checkstyle.checks.whitespace.PadOption;
import com.puppycrawl.tools.checkstyle.checks.whitespace.ParenPadCheck;

public class XpathRegressionParenPadTest extends AbstractXpathTestSupport {

    private final String checkName = ParenPadCheck.class.getSimpleName();

    @Override
    protected String getCheckName() {
        return checkName;
    }

    @Test
    public void testLeftFollowed() throws Exception {
        final File fileToProcess =
                new File(getPath("InputXpathParenPadLeftFollowed.java"));

        final DefaultConfiguration moduleConfig =
                createModuleConfig(ParenPadCheck.class);

        final String[] expectedViolation = {
            "5:12: " + getCheckMessage(ParenPadCheck.class,
                    AbstractParenPadCheck.MSG_WS_FOLLOWED, "("),
        };

        final List<String> expectedXpathQueries = Collections.singletonList(
            "/COMPILATION_UNIT/CLASS_DEF"
                + "[./IDENT[@text='InputXpathParenPadLeftFollowed']]"
                + "/OBJBLOCK/METHOD_DEF[./IDENT[@text='method']]/SLIST/LITERAL_IF/LPAREN"
        );

        runVerifications(moduleConfig, fileToProcess, expectedViolation,
                expectedXpathQueries);
    }

    @Test
    public void testLeftNotFollowed() throws Exception {
        final File fileToProcess =
                new File(getPath("InputXpathParenPadLeftNotFollowed.java"));

        final DefaultConfiguration moduleConfig =
                createModuleConfig(ParenPadCheck.class);
        moduleConfig.addProperty("option", PadOption.SPACE.toString());

        final String[] expectedViolation = {
            "5:12: " + getCheckMessage(ParenPadCheck.class,
                    AbstractParenPadCheck.MSG_WS_NOT_FOLLOWED, "("),
        };

        final List<String> expectedXpathQueries = Collections.singletonList(
            "/COMPILATION_UNIT/CLASS_DEF"
                + "[./IDENT[@text='InputXpathParenPadLeftNotFollowed']]"
                + "/OBJBLOCK/METHOD_DEF[./IDENT[@text='method']]/SLIST/LITERAL_IF/LPAREN"
        );

        runVerifications(moduleConfig, fileToProcess, expectedViolation,
                expectedXpathQueries);
    }

    @Test
    public void testRightPreceded() throws Exception {
        final File fileToProcess =
                new File(getPath("InputXpathParenPadRightPreceded.java"));

        final DefaultConfiguration moduleConfig =
                createModuleConfig(ParenPadCheck.class);

        final String[] expectedViolation = {
            "5:19: " + getCheckMessage(ParenPadCheck.class,
                    AbstractParenPadCheck.MSG_WS_PRECEDED, ")"),
        };

        final List<String> expectedXpathQueries = Collections.singletonList(
            "/COMPILATION_UNIT/CLASS_DEF"
                + "[./IDENT[@text='InputXpathParenPadRightPreceded']]"
                + "/OBJBLOCK/METHOD_DEF[./IDENT[@text='method']]/SLIST/LITERAL_IF/RPAREN"
        );

        runVerifications(moduleConfig, fileToProcess, expectedViolation,
                expectedXpathQueries);
    }

    @Test
    public void testRightNotPreceded() throws Exception {
        final File fileToProcess =
                new File(getPath("InputXpathParenPadRightNotPreceded.java"));

        final DefaultConfiguration moduleConfig =
                createModuleConfig(ParenPadCheck.class);
        moduleConfig.addProperty("option", PadOption.SPACE.toString());

        final String[] expectedViolation = {
            "5:19: " + getCheckMessage(ParenPadCheck.class,
                    AbstractParenPadCheck.MSG_WS_NOT_PRECEDED, ")"),
        };

        final List<String> expectedXpathQueries = Collections.singletonList(
            "/COMPILATION_UNIT/CLASS_DEF"
                + "[./IDENT[@text='InputXpathParenPadRightNotPreceded']]"
                + "/OBJBLOCK/METHOD_DEF[./IDENT[@text='method']]/SLIST/LITERAL_IF/RPAREN"
        );

        runVerifications(moduleConfig, fileToProcess, expectedViolation,
                expectedXpathQueries);
    }

}

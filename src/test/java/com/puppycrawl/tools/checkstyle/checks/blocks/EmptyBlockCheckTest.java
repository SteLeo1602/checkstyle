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

package com.puppycrawl.tools.checkstyle.checks.blocks;

import static com.google.common.truth.Truth.assertWithMessage;
import static com.puppycrawl.tools.checkstyle.checks.blocks.EmptyBlockCheck.MSG_KEY_BLOCK_EMPTY;
import static com.puppycrawl.tools.checkstyle.checks.blocks.EmptyBlockCheck.MSG_KEY_BLOCK_NO_STATEMENT;

import org.junit.jupiter.api.Test;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

public class EmptyBlockCheckTest
    extends AbstractModuleTestSupport {

    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/checks/blocks/emptyblock";
    }

    /* Additional test for jacoco, since valueOf()
     * is generated by javac and jacoco reports that
     * valueOf() is uncovered.
     */
    @Test
    public void testBlockOptionValueOf() {
        final BlockOption option = BlockOption.valueOf("TEXT");
        assertWithMessage("Invalid valueOf result")
            .that(option)
            .isEqualTo(BlockOption.TEXT);
    }

    @Test
    public void testDefault()
            throws Exception {
        final String[] expected = {
            "38:13: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "40:17: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "42:13: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "45:17: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "68:5: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "76:29: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "78:41: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "89:12: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
        };
        verifyWithInlineConfigParser(
                getPath("InputEmptyBlockSemantic.java"), expected);
    }

    @Test
    public void testText()
            throws Exception {
        final String[] expected = {
            "38:13: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "try"),
            "40:17: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "finally"),
            "68:5: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "INSTANCE_INIT"),
            "76:29: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "synchronized"),
            "88:12: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "STATIC_INIT"),
        };
        verifyWithInlineConfigParser(
                getPath("InputEmptyBlockSemanticText.java"), expected);
    }

    @Test
    public void testStatement()
            throws Exception {
        final String[] expected = {
            "38:13: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "40:17: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "42:13: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "45:17: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "68:5: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "76:29: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "78:41: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "89:12: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
        };
        verifyWithInlineConfigParser(
                getPath("InputEmptyBlockSemanticStatement.java"), expected);
    }

    @Test
    public void allowEmptyLoops() throws Exception {
        final String[] expected = {
            "21:21: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "24:34: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "27:21: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
            "28:20: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT),
        };
        verifyWithInlineConfigParser(
                getPath("InputEmptyBlockSemantic2Statement.java"), expected);
    }

    @Test
    public void allowEmptyLoopsText() throws Exception {
        final String[] expected = {
            "26:21: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "if"),
            "29:34: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "if"),
            "32:21: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "if"),
            "33:20: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "switch"),
        };
        verifyWithInlineConfigParser(
                getPath("InputEmptyBlockSemantic2Text.java"), expected);
    }

    @Test
    public void testInvalidOption() throws Exception {

        try {
            final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

            verifyWithInlineConfigParser(
                    getPath("InputEmptyBlockSemanticInvalid.java"), expected);
            assertWithMessage("exception expected").fail();
        }
        catch (CheckstyleException exc) {
            assertWithMessage("Invalid exception message")
                .that(exc.getMessage())
                .isEqualTo("cannot initialize module com.puppycrawl.tools.checkstyle.TreeWalker - "
                        + "cannot initialize module com.puppycrawl.tools.checkstyle.checks."
                        + "blocks.EmptyBlockCheck - "
                        + "Cannot set property 'option' to 'invalid_option'");
        }
    }

    @Test
    public void testAllowEmptyCaseWithText() throws Exception {
        final String[] expected = {
            "16:28: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "case"),
            "22:13: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "case"),
            "33:29: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "case"),
            "35:37: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "case"),
            "36:29: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "case"),
        };
        verifyWithInlineConfigParser(
                getPath("InputEmptyBlockCase.java"), expected);
    }

    @Test
    public void testForbidCaseWithoutStmt() throws Exception {
        final String[] expected = {
            "16:28: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "case"),
            "22:13: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "case"),
            "26:13: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "case"),
            "33:29: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "case"),
            "35:37: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "case"),
            "36:29: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "case"),
            "36:40: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "case"),
        };
        verifyWithInlineConfigParser(
                getPath("InputEmptyBlockCase2.java"), expected);
    }

    @Test
    public void testAllowEmptyDefaultWithText() throws Exception {
        final String[] expected = {
            "15:30: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "default"),
            "21:13: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "default"),
            "46:22: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "default"),
            "54:47: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "default"),
            "60:22: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "default"),
            "88:13: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "default"),
        };
        verifyWithInlineConfigParser(
                getPath("InputEmptyBlockDefault.java"), expected);
    }

    @Test
    public void testForbidDefaultWithoutStatement() throws Exception {
        final String[] expected = {
            "15:30: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "default"),
            "21:13: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "default"),
            "25:13: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "default"),
            "36:30: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "default"),
            "46:22: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "default"),
            "54:47: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "default"),
            "60:22: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "default"),
            "75:22: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "default"),
            "88:13: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "default"),
        };
        verifyWithInlineConfigParser(
                getPath("InputEmptyBlockDefault2.java"), expected);
    }

    @Test
    public void testEmptyBlockWithEmoji() throws Exception {
        final String[] expected = {
            "15:12: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "STATIC_INIT"),
            "25:27: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "if"),
            "28:34: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "if"),
            "30:62: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "for"),
            "31:25: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "if"),
            "33:25: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "switch"),
            "40:22: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "case"),
            "45:46: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "default"),
        };
        verifyWithInlineConfigParser(
                getPath("InputEmptyBlockWithEmoji.java"), expected);

    }

    @Test
    public void testAnnotationDefaultKeyword() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        final String path = getPath("InputEmptyBlockAnnotationDefaultKeyword.java");
        verifyWithInlineConfigParser(
                path, expected);
    }

    @Test
    public void testEmptyBlockSwitchExpressionsOne() throws Exception {
        final String[] expected = {
            "17:30: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "default"),
        };
        verifyWithInlineConfigParser(
                getPath("InputEmptyBlockSwitchExpressionsOne.java"), expected);
    }

    @Test
    public void testEmptyBlockSwitchExpressionsTwo() throws Exception {
        final String[] expected = {
            "25:32: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "case"),
            "27:26: " + getCheckMessage(MSG_KEY_BLOCK_NO_STATEMENT, "case"),
        };
        verifyWithInlineConfigParser(
                getPath("InputEmptyBlockSwitchExpressionsTwo.java"), expected);
    }

    @Test
    public void testUppercaseProperty() throws Exception {
        final String[] expected = {
            "16:30: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "default"),
            "22:13: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "default"),
        };
        verifyWithInlineConfigParser(
                getPath("InputEmptyBlockTestUppercaseOptionProperty.java"), expected);
    }

    @Test
    public void testEmptyBlockCaseAndDefaultWithTextOption() throws Exception {
        final String[] expected = {
            "20:28: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "case"),
            "24:22: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "default"),
            "33:30: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "case"),
            "37:24: " + getCheckMessage(MSG_KEY_BLOCK_EMPTY, "default"),
        };
        verifyWithInlineConfigParser(
                getNonCompilablePath("InputEmptyBlockCaseAndDefaultWithTextOption.java"),
                expected);
    }
}

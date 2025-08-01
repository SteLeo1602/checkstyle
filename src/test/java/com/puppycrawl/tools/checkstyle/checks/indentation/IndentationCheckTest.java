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

package com.puppycrawl.tools.checkstyle.checks.indentation;

import static com.google.common.truth.Truth.assertWithMessage;
import static com.puppycrawl.tools.checkstyle.checks.indentation.IndentationCheck.MSG_CHILD_ERROR;
import static com.puppycrawl.tools.checkstyle.checks.indentation.IndentationCheck.MSG_CHILD_ERROR_MULTI;
import static com.puppycrawl.tools.checkstyle.checks.indentation.IndentationCheck.MSG_ERROR;
import static com.puppycrawl.tools.checkstyle.checks.indentation.IndentationCheck.MSG_ERROR_MULTI;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Unit test for IndentationCheck.
 */
public class IndentationCheckTest extends AbstractModuleTestSupport {

    private static final Pattern LINE_WITH_COMMENT_REGEX = Pattern.compile(
        ".*?//(?:below )?indent:(\\d+)(?:"
                + " ioffset:(\\d+))? exp:(>=)?(\\d+(?:,\\d+)*?)( warn)?$");

    private static final IndentComment[] EMPTY_INDENT_COMMENT_ARRAY = new IndentComment[0];

    private static IndentComment[] getLinesWithWarnAndCheckComments(String aFileName,
            final int tabWidth)
            throws IOException {
        final List<IndentComment> result = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Path.of(aFileName),
                StandardCharsets.UTF_8)) {
            int lineNumber = 1;
            String line = br.readLine();
            IndentComment pendingBelowComment = null;

            while (line != null) {
                final Matcher match = LINE_WITH_COMMENT_REGEX.matcher(line);
                if (pendingBelowComment != null) {
                    final int actualIndent = getLineStart(line, tabWidth);

                    processPendingBelowComment(pendingBelowComment, actualIndent,
                            lineNumber, result);

                    pendingBelowComment = null;
                }
                else if (match.matches()) {
                    final boolean isBelow = line.contains("//below indent:");
                    final IndentComment warn = new IndentComment(match, lineNumber);

                    if (isBelow) {
                        pendingBelowComment = warn;
                    }
                    else {
                        final int actualIndent = getLineStart(line, tabWidth);
                        processInlineComment(warn, actualIndent, lineNumber, aFileName, result);
                    }
                }
                else if (!line.isEmpty()) {
                    throw new IllegalStateException(String.format(Locale.ROOT,
                            "File \"%1$s\" has no indentation comment or its format "
                                    + "malformed. Error on line: %2$d",
                            aFileName,
                            lineNumber));
                }

                line = br.readLine();
                lineNumber++;
            }
        }

        return result.toArray(EMPTY_INDENT_COMMENT_ARRAY);
    }

    private static void processPendingBelowComment(IndentComment pendingBelowComment,
            int actualIndent, int lineNumber, List<IndentComment> result) {
        if (actualIndent != pendingBelowComment.getIndent()) {
            throw new IllegalStateException(String.format(Locale.ROOT,
                    "Incorrect indentation in 'below' comment. "
                            + "Line %1$d (from line %2$d): comment:%3$d, actual:%4$d.",
                    lineNumber,
                    pendingBelowComment.getLineNumber(),
                    pendingBelowComment.getIndent(),
                    actualIndent));
        }

        if (!isCommentConsistent(pendingBelowComment)) {
            throw new IllegalStateException(String.format(Locale.ROOT,
                    "Inconsistent 'below' comment on line %1$d",
                    pendingBelowComment.getLineNumber()));
        }

        if (pendingBelowComment.isWarning()) {
            result.add(new IndentComment(pendingBelowComment, lineNumber));
        }
    }

    private static void processInlineComment(IndentComment warn, int actualIndent,
        int lineNumber, String fileName, List<IndentComment> result) {

        if (actualIndent != warn.getIndent()) {
            throw new IllegalStateException(String.format(Locale.ROOT,
                    "File \"%1$s\" has incorrect indentation in comment. "
                            + "Line %2$d: comment:%3$d, actual:%4$d.",
                    fileName,
                    lineNumber,
                    warn.getIndent(),
                    actualIndent));
        }

        if (!isCommentConsistent(warn)) {
            throw new IllegalStateException(String.format(Locale.ROOT,
                    "File \"%1$s\" has inconsistent comment on line %2$d",
                    fileName,
                    lineNumber));
        }

        if (warn.isWarning()) {
            result.add(warn);
        }
    }

    private static boolean isCommentConsistent(IndentComment comment) {
        final String[] levels = comment.getExpectedWarning().split(", ");
        final int indent = comment.getIndent() + comment.getIndentOffset();
        final boolean result;
        if (levels.length > 1) {
            // multi
            final boolean containsActualLevel =
                            Arrays.asList(levels).contains(String.valueOf(indent));

            result = containsActualLevel != comment.isWarning();
        }
        else {
            final int expectedWarning = Integer.parseInt(comment.getExpectedWarning());

            final boolean test;
            if (comment.isExpectedNonStrict()) {
                // non-strict
                test = indent >= expectedWarning;
            }
            else {
                // single
                test = expectedWarning == indent;
            }
            result = test != comment.isWarning();

        }
        return result;
    }

    private static int getLineStart(String line, final int tabWidth) {
        int lineStart = 0;
        for (int index = 0; index < line.length(); ++index) {
            if (!Character.isWhitespace(line.charAt(index))) {
                lineStart = CommonUtil.lengthExpandedTabs(line, index, tabWidth);
                break;
            }
        }
        return lineStart;
    }

    private void verifyWarns(Configuration config, String filePath,
                    String... expected)
                    throws Exception {
        final int tabWidth = Integer.parseInt(config.getProperty("tabWidth"));
        final IndentComment[] linesWithWarn =
                        getLinesWithWarnAndCheckComments(filePath, tabWidth);
        verify(config, filePath, expected, linesWithWarn);
        assertWithMessage("Expected warning count in UT does not match warn comment count "
                + "in input file")
            .that(expected.length)
            .isEqualTo(linesWithWarn.length);
    }

    private void verify(Configuration config, String filePath, String[] expected,
            final IndentComment... linesWithWarn) throws Exception {
        final Checker checker = createChecker(config);
        checker.addListener(new IndentAudit(linesWithWarn));
        verify(checker, filePath, expected);
    }

    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/checks/indentation/indentation";
    }

    @Test
    public void testGetRequiredTokens() {
        final IndentationCheck checkObj = new IndentationCheck();
        final int[] requiredTokens = checkObj.getRequiredTokens();
        final HandlerFactory handlerFactory = new HandlerFactory();
        final int[] expected = handlerFactory.getHandledTypes();
        Arrays.sort(expected);
        Arrays.sort(requiredTokens);
        assertWithMessage("Default required tokens are invalid")
            .that(requiredTokens)
            .isEqualTo(expected);
    }

    @Test
    public void testGetAcceptableTokens() {
        final IndentationCheck checkObj = new IndentationCheck();
        final int[] acceptableTokens = checkObj.getAcceptableTokens();
        final HandlerFactory handlerFactory = new HandlerFactory();
        final int[] expected = handlerFactory.getHandledTypes();
        Arrays.sort(expected);
        Arrays.sort(acceptableTokens);
        assertWithMessage("Default acceptable tokens are invalid")
            .that(acceptableTokens)
            .isEqualTo(expected);
    }

    @Test
    public void testThrowsIndentProperty() {
        final IndentationCheck indentationCheck = new IndentationCheck();

        indentationCheck.setThrowsIndent(1);

        assertWithMessage("Invalid throws indent")
            .that(indentationCheck.getThrowsIndent())
            .isEqualTo(1);
    }

    @Test
    public void testStrictCondition() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "4");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "8");
        final String[] expected = {
            "10:29: " + getCheckMessage(MSG_ERROR_MULTI, "method def rcurly", 28, "16, 20, 24"),
            "13:9: " + getCheckMessage(MSG_ERROR, "method def rcurly", 8, 4),
            "14:5: " + getCheckMessage(MSG_ERROR, "class def rcurly", 4, 0),
        };
        verifyWarns(checkConfig, getPath("InputIndentationStrictCondition.java"), expected);
    }

    @Test
    public void forbidOldStyle() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "8");
        final String[] expected = {
            "20:30: " + getCheckMessage(MSG_ERROR, "int", 29, 12),
            "21:30: " + getCheckMessage(MSG_ERROR, "int", 29, 12),
        };
        verifyWarns(checkConfig, getPath("InputIndentationMethodCStyle.java"), expected);
    }

    @Test
    public void testZeroCaseLevel() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "0");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationZeroCaseLevel.java"), expected);
    }

    @Test
    public void testAndroidStyle() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "8");
        final String[] expected = {
            "42:4: " + getCheckMessage(MSG_ERROR, "extends", 3, 8),
            "44:4: " + getCheckMessage(MSG_ERROR, "member def type", 3, 4),
            "47:9: " + getCheckMessage(MSG_ERROR, "foo", 8, 12),
            "50:9: " + getCheckMessage(MSG_ERROR, "int", 8, 12),
            "53:14: " + getCheckMessage(MSG_ERROR, "true", 13, 16),
            "56:17: " + getCheckMessage(MSG_ERROR, "+", 16, 20),
            "57:9: " + getCheckMessage(MSG_ERROR, "if", 8, 12),
            "60:12: " + getCheckMessage(MSG_ERROR, "if rcurly", 11, 12),
            "62:8: " + getCheckMessage(MSG_CHILD_ERROR, "method def", 7, 8),
        };
        verifyWarns(checkConfig, getPath("InputIndentationAndroidStyle.java"), expected);
    }

    @Test
    public void testMethodCallLineWrap() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = {
            "53:19: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 18, 20),
            "54:15: " + getCheckMessage(MSG_ERROR, "method call rparen", 14, 16),
            "75:13: " + getCheckMessage(MSG_ERROR, "lambda arguments", 12, 16),
        };
        verifyWarns(checkConfig, getPath("InputIndentationMethodCallLineWrap.java"), expected);
    }

    @Test
    public void testDifficultAnnotations() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = {
            "40:1: " + getCheckMessage(MSG_CHILD_ERROR_MULTI,
                    "annotation array initialization", 0, "4, 23, 25"),
            "41:1: " + getCheckMessage(MSG_CHILD_ERROR_MULTI,
                    "annotation array initialization", 0, "4, 23, 25"),
            "50:7: " + getCheckMessage(MSG_CHILD_ERROR_MULTI,
                    "annotation array initialization", 6, "8, 27, 29"),
        };
        verifyWarns(checkConfig, getPath("InputIndentationDifficultAnnotations.java"), expected);
    }

    @Test
    public void testAnnotationClosingParenthesisEndsInSameIndentationAsOpening() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("tabWidth", "4");

        final String[] expected = {
            "34:17: " + getCheckMessage(MSG_ERROR, ")", 16, 0),
            "36:17: " + getCheckMessage(MSG_ERROR, ")", 16, 0),
            "40:9: " + getCheckMessage(MSG_ERROR, ")", 8, 4),
            "42:9: " + getCheckMessage(MSG_ERROR, ")", 8, 4),
            "46:9: " + getCheckMessage(MSG_ERROR, ")", 8, 4),
        };

        verifyWarns(checkConfig,
            getPath("InputIndentation"
                + "AnnotationClosingParenthesisEndsInSameIndentationAsOpening.java"),
                expected);
    }

    @Test
    public void testAnnotationsFromGuava() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationFromGuava.java"), expected);
    }

    @Test
    public void testAnnotationsFromGuava1() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationFromGuava1.java"), expected);
    }

    @Test
    public void testAnnotationsFromGuava2() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationFromGuava2.java"), expected);
    }

    @Test
    public void testAnnotationsFromGuava3() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationFromGuava3.java"), expected);
    }

    @Test
    public void testAnnotationsFromGuava4() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationFromGuava4.java"), expected);
    }

    @Test
    public void testCorrectIfAndParameters() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = {
            "65:15: " + getCheckMessage(MSG_ERROR_MULTI, "new", 14, "16, 18"),
            "72:19: " + getCheckMessage(MSG_ERROR_MULTI, "new", 18, "40, 42"),
            "92:17: " + getCheckMessage(MSG_ERROR_MULTI, "new", 16, "18, 20"),
            "96:11: " + getCheckMessage(MSG_ERROR, "+", 10, 12),
            "99:31: " + getCheckMessage(MSG_ERROR_MULTI, "new", 30, "38, 40"),
            "101:17: " + getCheckMessage(MSG_CHILD_ERROR, "new", 16, 44),
        };
        verifyWarns(checkConfig, getPath("InputIndentationIfAndParameter.java"), expected);
    }

    @Test
    public void testCorrectIfAndParameters1() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = {
            "38:13: " + getCheckMessage(MSG_ERROR_MULTI, "new", 12, "14, 16"),
            "42:17: " + getCheckMessage(MSG_CHILD_ERROR, "new", 16, 28),
            "45:25: " + getCheckMessage(MSG_ERROR_MULTI, "new", 24, "42, 44"),
        };
        verifyWarns(checkConfig, getPath("InputIndentationCorrectIfAndParameter1.java"), expected);
    }

    @Test
    public void testAnonymousClasses() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationAnonymousClasses.java"), expected);
    }

    @Test
    public void testArrays() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "2");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationArrays.java"), expected);
    }

    @Test
    public void testLabels() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationLabels.java"), expected);
    }

    @Test
    public void testLabels1() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationLabels1.java"), expected);
    }

    @Test
    public void testClassesAndMethods() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationClassesMethods.java"), expected);
    }

    @Test
    public void testCtorCall() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = {
            "28:5: " + getCheckMessage(MSG_CHILD_ERROR, "ctor def", 4, 6),
            "29:5: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 4, 6),
            "30:5: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 4, 6),
            "34:5: " + getCheckMessage(MSG_CHILD_ERROR, "ctor def", 4, 6),
            "35:5: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 4, 6),
            "39:5: " + getCheckMessage(MSG_CHILD_ERROR, "ctor def", 4, 6),
            "40:5: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 4, 8),
            "41:5: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 4, 8),
            "45:5: " + getCheckMessage(MSG_CHILD_ERROR, "ctor def", 4, 6),
            "46:5: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 4, 8),
            "50:5: " + getCheckMessage(MSG_CHILD_ERROR, "ctor def", 4, 6),
            "51:5: " + getCheckMessage(MSG_ERROR, "(", 4, 8),
            "52:5: " + getCheckMessage(MSG_ERROR, "x", 4, 8),
            "56:5: " + getCheckMessage(MSG_CHILD_ERROR, "ctor def", 4, 6),
            "57:5: " + getCheckMessage(MSG_ERROR, "method call lparen", 4, 6),
            "62:5: " + getCheckMessage(MSG_ERROR, ".", 4, 10),
            "63:5: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 4, 8),
            "68:5: " + getCheckMessage(MSG_ERROR, "super", 4, 10),
            "69:5: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 4, 8),
            "75:11: " + getCheckMessage(MSG_ERROR_MULTI, "lambda arguments", 10, "12, 14"),
        };
        verifyWarns(checkConfig, getPath("InputIndentationCtorCall.java"), expected);
    }

    @Test
    public void testCtorCall1() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationCtorCall1.java"), expected);
    }

    @Test
    public void testMembers() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = {
            "22:6: " + getCheckMessage(MSG_ERROR, "=", 5, 6),
            "57:4: " + getCheckMessage(MSG_ERROR, "class def rcurly", 3, 2),
        };

        verifyWarns(checkConfig, getPath("InputIndentationMembers.java"), expected);
    }

    @Test
    public void testAnnotationArrayInit() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "6");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "8");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = {

            "17:1: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization", 0,
                "4, 6, 34, 36"),
            "22:14: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization",
                    13, "4, 6, 34, 36"),
            "23:3: " + getCheckMessage(MSG_ERROR_MULTI,
                    "annotation array initialization rcurly", 2, "0, 4"),
            "35:7: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization", 6,
                "8, 10, 31, 33"),
            "36:3: " + getCheckMessage(MSG_ERROR_MULTI,
                    "annotation array initialization rcurly", 2, "4, 8"),

            "52:6: " + getCheckMessage(MSG_CHILD_ERROR_MULTI,
                    "annotation array initialization", 5, "6, 8, 10"),
            "54:6: " + getCheckMessage(MSG_ERROR_MULTI,
                    "annotation array initialization rcurly", 5, "2, 6"),
        };
        final String fileName = getPath("InputIndentationAnnArrInit.java");
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testAnnotationArrayInitTwo() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "0");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "0");
        checkConfig.addProperty("tabWidth", "8");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = {

            "17:5: " + getCheckMessage(MSG_CHILD_ERROR_MULTI,
                "annotation array initialization", 4, "0, 33, 35"),
            "30:9: " + getCheckMessage(MSG_CHILD_ERROR_MULTI,
                "annotation array initialization", 8, "4, 29, 31"),
            "32:3: " + getCheckMessage(MSG_ERROR,
                "annotation array initialization rcurly", 2, 4),
            "47:7: " + getCheckMessage(MSG_ERROR,
                "annotation array initialization lcurly", 6, 2),
            "49:5: " + getCheckMessage(MSG_CHILD_ERROR_MULTI,
                "annotation array initialization", 4, "2, 6, 8"),
        };
        final String fileName = getPath("InputIndentationAnnArrInit2.java");
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testAnnotationArrayInitWithEmoji() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "0");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "0");
        checkConfig.addProperty("tabWidth", "8");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = {
            "17:5: " + getCheckMessage(MSG_CHILD_ERROR_MULTI,
                    "annotation array initialization", 4, "0, 41, 43"),
            "30:9: " + getCheckMessage(MSG_CHILD_ERROR_MULTI,
                    "annotation array initialization", 8, "4, 29, 31"),
            "32:3: " + getCheckMessage(MSG_ERROR,
                    "annotation array initialization rcurly", 2, 4),
            "42:7: " + getCheckMessage(MSG_ERROR,
                    "member def type", 6, "4"),
            "47:7: " + getCheckMessage(MSG_ERROR,
                    "annotation array initialization lcurly", 6, "2"),
            "48:11: " + getCheckMessage(MSG_CHILD_ERROR_MULTI,
                    "annotation array initialization", 10, "2, 6, 8"),
            "49:13: " + getCheckMessage(MSG_CHILD_ERROR_MULTI,
                    "annotation array initialization", 12, "2, 6, 8"),
            "50:21: " + getCheckMessage(MSG_CHILD_ERROR_MULTI,
                    "annotation array initialization", 20, "2, 6, 8"),
            "52:5: " + getCheckMessage(MSG_CHILD_ERROR_MULTI,
                    "annotation array initialization", 4, "2, 6, 8"),
        };
        final String fileName = getPath("InputIndentationAnnArrInitWithEmoji.java");
        verifyWarns(checkConfig, fileName, expected);

    }

    @Test
    public void testOddAnnotations()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "3");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");

        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "9");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationOddLineWrappingAndArrayInit.java");
        final String[] expected = {
            "25:17: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization",
                    16, "11, 17, 47, 54"),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testAnnotationOddStyles() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("tabWidth", "8");

        final String fileName = getPath("InputIndentationAnnotationArrayInitOldStyle.java");

        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testZeroAnnotationArrayInit()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "0");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationZeroArrayInit.java");

        final String[] expected = {
            "22:12: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization",
                    11, "8, 12, 35, 37"),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testAnnotationArrayInitGoodCase()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationAnnotationArrayInitGood.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testAnnotationArrayInitGoodCaseTwo()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "2");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationAnnotationArrayInitGood.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidLabel() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = {
            "24:11: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "label", 10, "8, 12"),
            "33:3: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "label", 2, "4, 8"),
            "36:19: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "label", 18, "8, 12"),
            "37:19: " + getCheckMessage(MSG_CHILD_ERROR, "ctor def", 18, 8),
            "39:7: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "label", 6, "8, 12"),
            "41:7: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "label", 6, "8, 12"),
        };
        verifyWarns(checkConfig, getPath("InputIndentationInvalidLabelIndent.java"), expected);
    }

    @Test
    public void testInvalidLabelWithWhileLoop() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = {
            "18:10: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "label", 9, "4, 8"),
            "19:10: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "label", 9, "8, 12"),
        };
        verifyWarns(checkConfig, getPath("InputIndentationInvalidLabelWithWhileLoopIndent.java"),
            expected);
    }

    @Test
    public void testValidLabel() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationValidLabelIndent.java"), expected);
    }

    @Test
    public void testValidIfWithChecker() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidIfIndent.java");
        final String[] expected = {
            "95:9: " + getCheckMessage(MSG_ERROR, "(", 8, 12),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidIfWithChecker1() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidIfIndent1.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidIfWithChecker2() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidIfIndent2.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidDotWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidDotIndent.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidMethodWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidMethodIndent.java");
        final String[] expected = {
            "76:5: " + getCheckMessage(MSG_ERROR, "void", 4, 8),
            "77:5: " + getCheckMessage(MSG_ERROR, "method5", 4, 8),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidMethodWithChecker1()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidMethodIndent1.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidMethodWithChecker1()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidMethodIndent1.java");
        final String[] expected = {
            "23:7: " + getCheckMessage(MSG_ERROR, "ctor def rcurly", 6, 4),
            "26:7: " + getCheckMessage(MSG_ERROR, "ctor def modifier", 6, 4),
            "27:3: " + getCheckMessage(MSG_ERROR, "ctor def lcurly", 2, 4),
            "28:7: " + getCheckMessage(MSG_ERROR, "ctor def rcurly", 6, 4),
            "31:3: " + getCheckMessage(MSG_ERROR, "method def modifier", 2, 4),
            "32:7: " + getCheckMessage(MSG_ERROR, "method def rcurly", 6, 4),
            "69:6: " + getCheckMessage(MSG_ERROR, "method def modifier", 5, 4),
            "70:6: " + getCheckMessage(MSG_ERROR, "final", 5, 9),
            "71:6: " + getCheckMessage(MSG_ERROR, "void", 5, 9),
            "72:5: " + getCheckMessage(MSG_ERROR, "method5", 4, 9),
            "86:11: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 10, 12),
            "89:11: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 10, 12),
            "99:7: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 6, 12),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidMethodWithChecker2()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidMethodIndent2.java");
        final String[] expected = {
            "23:5: " + getCheckMessage(MSG_CHILD_ERROR, "ctor def", 4, 8),
            "26:4: " + getCheckMessage(MSG_ERROR, "method def modifier", 3, 4),
            "27:4: " + getCheckMessage(MSG_ERROR, "final", 3, 7),
            "28:4: " + getCheckMessage(MSG_ERROR, "void", 3, 7),
            "29:6: " + getCheckMessage(MSG_ERROR, "method6", 5, 7),
            "39:7: " + getCheckMessage(MSG_CHILD_ERROR, "method def", 6, 8),
            "40:7: " + getCheckMessage(MSG_ERROR, "if", 6, 8),
            "41:11: " + getCheckMessage(MSG_CHILD_ERROR, "if", 10, 12),
            "42:7: " + getCheckMessage(MSG_ERROR, "if rcurly", 6, 8),
            "45:11: " + getCheckMessage(MSG_ERROR, "Arrays", 10, 12),
            "51:15: " + getCheckMessage(MSG_ERROR, "new", 14, 16),
            "54:11: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 10, 12),
            "59:15: " + getCheckMessage(MSG_ERROR, "new", 14, 16),
            "63:11: " + getCheckMessage(MSG_ERROR, "new", 10, 12),
            "67:11: " + getCheckMessage(MSG_ERROR, "new", 10, 12),
            "68:7: " + getCheckMessage(MSG_ERROR, ")", 6, 8),
            "72:7: " + getCheckMessage(MSG_ERROR, "method call rparen", 6, 8),
            "86:5: " + getCheckMessage(MSG_CHILD_ERROR, "method def", 4, 8),
            "91:5: " + getCheckMessage(MSG_CHILD_ERROR, "method def", 4, 8),
            "95:1: " + getCheckMessage(MSG_ERROR, "int", 0, 8),
            "96:5: " + getCheckMessage(MSG_ERROR, "method9", 4, 8),
            "106:13: " + getCheckMessage(MSG_CHILD_ERROR, "method def", 12, 8),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testAlternativeGoogleStyleSwitchCaseAndEnums()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "2");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationSwitchCasesAndEnums.java");
        final String[] expected = {
            "18:7: " + getCheckMessage(MSG_CHILD_ERROR, "block", 6, 4),
            "35:7: " + getCheckMessage(MSG_ERROR, "block lcurly", 6, 8),
            "38:11: " + getCheckMessage(MSG_ERROR, "block rcurly", 10, 8),
            "54:5: " + getCheckMessage(MSG_ERROR, "block lcurly", 4, 2),
            "55:3: " + getCheckMessage(MSG_CHILD_ERROR, "block", 2, 4),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidSwitchWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidSwitchIndent.java");
        final String[] expected = {
            "30:7: " + getCheckMessage(MSG_ERROR, "switch", 6, 8),
            "32:11: " + getCheckMessage(MSG_CHILD_ERROR, "case", 10, 12),
            "33:15: " + getCheckMessage(MSG_CHILD_ERROR, "block", 14, 16),
            "37:15: " + getCheckMessage(MSG_CHILD_ERROR, "block", 14, 16),
            "39:15: " + getCheckMessage(MSG_CHILD_ERROR, "case", 14, 12),
            "40:11: " + getCheckMessage(MSG_CHILD_ERROR, "case", 10, 12),
            "43:11: " + getCheckMessage(MSG_CHILD_ERROR, "case", 10, 12),
            "44:15: " + getCheckMessage(MSG_CHILD_ERROR, "block", 14, 16),
            "45:15: " + getCheckMessage(MSG_CHILD_ERROR, "block", 14, 16),
            "53:15: " + getCheckMessage(MSG_CHILD_ERROR, "block", 14, 16),
            "54:19: " + getCheckMessage(MSG_CHILD_ERROR, "block", 18, 16),
            "55:11: " + getCheckMessage(MSG_ERROR, "block rcurly", 10, 12),
            "59:11: " + getCheckMessage(MSG_ERROR, "block lcurly", 10, 12),
            "62:15: " + getCheckMessage(MSG_ERROR, "block rcurly", 14, 12),
            "66:15: " + getCheckMessage(MSG_ERROR, "block lcurly", 14, 12),
            "69:11: " + getCheckMessage(MSG_ERROR, "block rcurly", 10, 12),
            "76:15: " + getCheckMessage(MSG_CHILD_ERROR, "case", 14, 16),
            "81:15: " + getCheckMessage(MSG_CHILD_ERROR, "case", 14, 16),
            "89:7: " + getCheckMessage(MSG_ERROR, "switch rcurly", 6, 8),
            "92:7: " + getCheckMessage(MSG_ERROR, "switch lcurly", 6, 8),
            "93:11: " + getCheckMessage(MSG_ERROR, "switch rcurly", 10, 8),
            "95:11: " + getCheckMessage(MSG_ERROR, "switch lcurly", 10, 8),
            "96:7: " + getCheckMessage(MSG_ERROR, "switch rcurly", 6, 8),
            "99:9: " + getCheckMessage(MSG_CHILD_ERROR, "case", 8, 12),
            "100:13: " + getCheckMessage(MSG_ERROR, "if", 12, 16),
            "101:17: " + getCheckMessage(MSG_CHILD_ERROR, "if", 16, 20),
            "102:13: " + getCheckMessage(MSG_ERROR, "else", 12, 16),
            "103:17: " + getCheckMessage(MSG_CHILD_ERROR, "else", 16, 20),
            "106:17: " + getCheckMessage(MSG_CHILD_ERROR, "case", 4, 12),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testIfElseWithNoCurly()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationIfElseWithNoCurly.java");
        final String[] expected = {
            "20:1: " + getCheckMessage(MSG_CHILD_ERROR, "if", 0, 12),
            "25:5: " + getCheckMessage(MSG_ERROR, "if", 4, 8),
            "26:9: " + getCheckMessage(MSG_CHILD_ERROR, "if", 8, 12),
            "37:13: " + getCheckMessage(MSG_ERROR, "else", 12, 8),
            "39:9: " + getCheckMessage(MSG_ERROR, "if", 8, 12),
            "43:9: " + getCheckMessage(MSG_CHILD_ERROR, "if", 8, 16),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testWhileWithNoCurly()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationWhileNoCurly.java");
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_CHILD_ERROR, "while", 0, 12),
            "26:5: " + getCheckMessage(MSG_ERROR, "while", 4, 8),
            "27:9: " + getCheckMessage(MSG_CHILD_ERROR, "while", 8, 12),
            "32:9: " + getCheckMessage(MSG_ERROR, "while", 8, 12),
            "36:9: " + getCheckMessage(MSG_CHILD_ERROR, "while", 8, 16),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testForWithNoCurly()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationForWithoutCurly.java");
        final String[] expected = {
            "21:1: " + getCheckMessage(MSG_CHILD_ERROR, "for", 0, 12),
            "26:5: " + getCheckMessage(MSG_ERROR, "for", 4, 8),
            "27:9: " + getCheckMessage(MSG_CHILD_ERROR, "for", 8, 12),
            "32:9: " + getCheckMessage(MSG_ERROR, "for", 8, 12),
            "33:9: " + getCheckMessage(MSG_CHILD_ERROR, "for", 8, 16),
            "37:9: " + getCheckMessage(MSG_CHILD_ERROR, "for", 8, 16),

        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testDoWhileWithoutCurly()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationDoWhile.java");
        final String[] expected = {
            "23:9: " + getCheckMessage(MSG_CHILD_ERROR, "do..while", 8, 12),
            "30:5: " + getCheckMessage(MSG_ERROR, "do..while while", 4, 8),
            "33:13: " + getCheckMessage(MSG_ERROR, "do..while while", 12, 8),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidSwitchWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidSwitchIndent.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testNewKeyword() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "8");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationNew.java"), expected);
    }

    @Test
    public void testNewKeywordChildren() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "2");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "2");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = {
            "27:1: " + getCheckMessage(MSG_ERROR_MULTI, "new", 0, "14, 16"),
            "28:1: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "new", 0, "18, 20"),
            "36:1: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "new", 0, "18, 20"),
            "42:9: " + getCheckMessage(MSG_ERROR, "new", 8, 12),
            "43:11: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "object def", 10, "14, 16, 18"),
            "51:1: " + getCheckMessage(MSG_CHILD_ERROR, "new", 0, 16),
            "56:5: " + getCheckMessage(MSG_CHILD_ERROR, "new", 4, 8),
            "57:5: " + getCheckMessage(MSG_CHILD_ERROR, "new", 4, 8),
            "63:5: " + getCheckMessage(MSG_ERROR_MULTI, "lambda arguments", 4, "10, 12"),
            "64:1: " + getCheckMessage(MSG_CHILD_ERROR, "new", 0, 8),
        };
        verifyWarns(checkConfig, getPath("InputIndentationNewChildren.java"), expected);
    }

    @Test
    public void testNewKeywordChildrenSevntuConfig() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "8");
        final String[] expected = {
            "43:29: " + getCheckMessage(MSG_CHILD_ERROR, "new", 28, 36),
        };
        verifyWarns(checkConfig,
                getPath("InputIndentationNewChildrenSevntuConfig.java"), expected);
    }

    @Test
    public void testNewKeyword2() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "8");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationNew.java"), expected);
    }

    // we can not use verifyWarns() due to usage of multi line string syntax in input
    @Test
    public void testTextBlockLiteral() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = {
            "18:1: " + getCheckMessage(MSG_ERROR, "\"\"\"", 0, 8),
            "29:17: " + getCheckMessage(MSG_ERROR, "\"\"\"", 16, 12),
            "46:1: " + getCheckMessage(MSG_ERROR, "\"\"\"", 0, 12),
            "52:1: " + getCheckMessage(MSG_ERROR, "\"\"\"", 0, 12),
            "59:9: " + getCheckMessage(MSG_ERROR, "\"\"\"", 8, 12),
            "78:15: " + getCheckMessage(MSG_ERROR, "\"\"\"", 14, 12),
        };
        verifyWarns(checkConfig, getPath("InputIndentationTextBlock.java"),
            expected);
    }

    @Test
    public void testValidNewKeywordWithForceStrictCondition() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "8");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationNew.java"), expected);
    }

    @Test
    public void testInvalidNewKeywordWithForceStrictCondition() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "8");
        final String[] expected = {
            "21:12: " + getCheckMessage(MSG_ERROR, "]", 11, 12),
            "25:5: " + getCheckMessage(MSG_ERROR, "[", 4, 12),
            "32:17: " + getCheckMessage(MSG_ERROR, "new", 16, 24),
            "33:21: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "object def", 20, "28, 32, 36"),
            "34:17: " + getCheckMessage(MSG_ERROR_MULTI, "object def rcurly", 16, "24, 28, 32"),
            "37:36: " + getCheckMessage(MSG_ERROR, "+", 35, 16),
            "41:35: " + getCheckMessage(MSG_ERROR, "]", 34, 16),
            "45:36: " + getCheckMessage(MSG_ERROR, "42", 35, 16),
            "49:36: " + getCheckMessage(MSG_ERROR, "+", 35, 16),
            "50:36: " + getCheckMessage(MSG_ERROR, "+", 35, 16),
            "55:21: " + getCheckMessage(MSG_ERROR, "1", 20, 16),
            "59:13: " + getCheckMessage(MSG_ERROR, "fun2", 12, 16),
            "78:11: " + getCheckMessage(MSG_ERROR, "Object", 10, 12),
            "82:16: " + getCheckMessage(MSG_ERROR, "]", 15, 12),
        };
        verifyWarns(checkConfig,
            getPath("InputIndentationNewWithForceStrictCondition.java"), expected);
    }

    @Test
    public void testValidArrayInitDefaultIndentWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidArrayInitDefaultIndent.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidArrayInitWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "8");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidArrayInitIndent.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidArrayInitTwoDimensional() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "2");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "4");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidArrayInitIndentTwoDimensional.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidArrayInitTwoDimensional() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "2");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "4");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName =
            getPath("InputIndentationInvalidArrayInitIndentTwoDimensional.java");
        final String[] expected = {
            "18:5: " + getCheckMessage(MSG_ERROR_MULTI,
                "array initialization lcurly", 4, "6, 8, 18, 20, 24"),
            "23:10: " + getCheckMessage(MSG_CHILD_ERROR_MULTI,
                "array initialization", 9, "8, 10, 12, 20, 22, 24"),
            "26:7: " + getCheckMessage(MSG_CHILD_ERROR_MULTI,
                "array initialization", 6, "8, 10, 12, 20, 22, 24"),
            "28:5: " + getCheckMessage(MSG_ERROR_MULTI,
                "array initialization lcurly", 4, "6, 8, 18, 20, 24"),
            "30:5: " + getCheckMessage(MSG_ERROR_MULTI,
                "array initialization rcurly", 4, "6, 8, 18, 20, 24"),

        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidArrayInit()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "2");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "2");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidArrayInitIndentTwo.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testArrayInitWithEmoji() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "2");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "2");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationArrayInitIndentWithEmoji.java");
        final String[] expected = {
            "19:6: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization",
               5, "4, 6, 52, 54"),
            "24:9: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization",
               8, "4, 6, 35, 37"),
            "25:11: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization",
               10, "4, 6, 35, 37"),
            "30:11: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization lcurly",
               10, "4, 6, 19, 21, 25"),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testYieldKeywordWithForceStrictCondition() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = {
            "15:13: " + getCheckMessage(MSG_CHILD_ERROR, "block", 12, 16),
            "16:13: " + getCheckMessage(MSG_ERROR, "yield", 12, 16),
            "44:13: " + getCheckMessage(MSG_CHILD_ERROR, "block", 12, 16),
            "45:13: " + getCheckMessage(MSG_ERROR, "yield", 12, 16),
            "50:5: " + getCheckMessage(MSG_ERROR, "yield", 4, 16),
            "71:15: " + getCheckMessage(MSG_ERROR, "yield", 14, 16),
            "74:20: " + getCheckMessage(MSG_ERROR, "yield", 19, 16),
            "77:9: " + getCheckMessage(MSG_ERROR, "yield", 8, 16),
        };
        verifyWarns(checkConfig,
                getPath("InputIndentationYieldForceStrict.java"), expected);
    }

    @Test
    public void testChainedMethodCalling() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "2");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "2");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationChainedMethodCalls.java");
        final String[] expected = {
            "32:5: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 4, 8),
            "37:5: " + getCheckMessage(MSG_ERROR, ".", 4, 8),
            "38:5: " + getCheckMessage(MSG_ERROR, ".", 4, 8),
            "41:5: " + getCheckMessage(MSG_ERROR, "new", 4, 8),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidArrayInitWithTrueStrictCondition()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidArrayInitIndent.java");
        final String[] expected = {
            "21:3: " + getCheckMessage(MSG_ERROR, "member def type", 2, 4),
            "22:7: " + getCheckMessage(MSG_ERROR, "member def type", 6, 4),
            "24:3: " + getCheckMessage(MSG_ERROR, "member def type", 2, 4),
            "28:7: " + getCheckMessage(MSG_ERROR, "member def type", 6, 4),
            "29:9: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 8,
                "10, 34, 36"),
            "30:5: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization rcurly", 4, "6, 10"),
            "33:10: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 9,
                "8, 31, 33"),
            "34:8: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 7,
                "8, 31, 33"),
            "35:10: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 9,
                "8, 31, 33"),
            "40:3: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization lcurly", 2, "4, 8"),
            "44:7: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization rcurly", 6, "4, 8"),
            "48:3: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization lcurly", 2, "4, 8"),
            "52:21: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 20,
                "8, 31, 33"),
            "53:5: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization",
                4, "8, 31, 33"),
            "58:7: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization",
                6, "8, 31, 33"),
            "63:3: " + getCheckMessage(MSG_ERROR, "member def type", 2, 4),
            "65:7: " + getCheckMessage(MSG_ERROR, "member def type", 6, 4),
            "66:3: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization rcurly", 2, "6, 10"),
            "69:7: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization",
                6, "8, 36, 38"),
            "76:11: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization",
                10, "12, 24, 26"),
        };

        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidArrayInitWithTrueStrictCondition1()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidArrayInitIndent1.java");
        final String[] expected = {
            "28:9: " + getCheckMessage(MSG_ERROR, "1", 8, 12),
            "39:11: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 10,
                "12, 30, 32"),
            "40:15: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 14,
                "12, 30, 32"),
            "43:11: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 10,
                "12, 31, 32"),
            "44:15: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 14,
                "12, 31, 32"),
            "45:7: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization rcurly", 6, "8, 12"),
            "48:7: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization lcurly", 6, "8, 12"),
            "49:15: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 14,
                "8, 10, 12"),
            "51:7: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization rcurly", 6, "8, 12"),
            // following are tests for annotation array initialization
            "59:13: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization",
                12, "16, 46, 48"),
            "67:15: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization",
                14, "16, 28, 30"),
            "68:9: " + getCheckMessage(MSG_ERROR_MULTI, "annotation array initialization rcurly",
                8, "12, 16"),
            "70:13: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization",
                12, "16, 29, 31"),
        };

        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidArrayInitWithFalseStrictCondition()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidArrayInitIndent.java");
        final String[] expected = {
            "21:3: " + getCheckMessage(MSG_ERROR, "member def type", 2, 4),
            "22:7: " + getCheckMessage(MSG_ERROR, "member def type", 6, 4),
            "24:3: " + getCheckMessage(MSG_ERROR, "member def type", 2, 4),
            "28:7: " + getCheckMessage(MSG_ERROR, "member def type", 6, 4),
            "29:9: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 8,
                "10, 34, 36"),
            "30:5: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization rcurly", 4, "6, 10"),
            "33:10: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 9,
                "8, 31, 33"),
            "34:8: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 7,
                "8, 31, 33"),
            "35:10: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 9,
                "8, 31, 33"),
            "40:3: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization lcurly", 2, "4, 8"),
            "44:7: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization rcurly", 6, "4, 8"),
            "48:3: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization lcurly", 2, "4, 8"),
            "52:21: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 20,
                "8, 31, 33"),
            "53:5: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization",
                4, "8, 31, 33"),
            "58:7: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization",
                6, "8, 31, 33"),
            "63:3: " + getCheckMessage(MSG_ERROR, "member def type", 2, 4),
            "65:7: " + getCheckMessage(MSG_ERROR, "member def type", 6, 4),
            "66:3: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization rcurly", 2, "6, 10"),
            "69:7: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization",
                6, "8, 36, 38"),
            "76:11: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization",
                10, "12, 24, 26"),
        };

        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidArrayInitWithFalseStrictCondition1()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidArrayInitIndent1.java");
        final String[] expected = {
            "28:9: " + getCheckMessage(MSG_ERROR, "1", 8, 12),
            "39:11: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 10,
                "12, 30, 32"),
            "40:15: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 14,
                "12, 30, 32"),
            "43:11: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 10,
                "12, 31, 32"),
            "44:15: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 14,
                "12, 31, 32"),
            "45:7: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization rcurly", 6, "8, 12"),
            "48:7: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization lcurly", 6, "8, 12"),
            "49:15: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "array initialization", 14,
                "8, 10, 12"),
            "51:7: " + getCheckMessage(MSG_ERROR_MULTI, "array initialization rcurly", 6, "8, 12"),
            // following are tests for annotation array initialization
            "59:13: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization",
                12, "16, 46, 48"),
            "67:15: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization",
                14, "16, 28, 30"),
            "68:9: " + getCheckMessage(MSG_ERROR_MULTI, "annotation array initialization rcurly",
                8, "12, 16"),
            "70:13: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization",
                12, "16, 29, 31"),
        };

        verifyWarns(checkConfig, fileName, expected);
    }

    // Test Input without trailing comment and usage of 'verify' method is due to #16906
    @Test
    public void testInvalidArrayInitIndentNoCommentsTrueStrictCondition()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath(
            "InputIndentationInvalidArrayInitIndentWithoutTrailingComments.java");
        final String[] expected = {
            "29:13: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization",
                12, "16, 46, 48"),
            "35:15: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization",
                14, "12, 16"),
            "39:15: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization",
                14, "16, 28, 30"),
            "40:9: " + getCheckMessage(MSG_ERROR_MULTI, "annotation array initialization rcurly",
                8, "12, 16"),
            "43:13: " + getCheckMessage(MSG_CHILD_ERROR, "annotation array initialization",
                12, 16),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    // Test Input without trailing comment and usage of 'verify' method is due to #16906
    @Test
    public void testInvalidArrayInitIndentNoCommentsFalseStrictCondition()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath(
            "InputIndentationInvalidArrayInitIndentWithoutTrailingComments.java");
        final String[] expected = {
            "29:13: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization",
                12, "16, 46, 48"),
            "35:15: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization",
                14, "12, 16"),
            "39:15: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "annotation array initialization",
                14, "16, 28, 30"),
            "40:9: " + getCheckMessage(MSG_ERROR_MULTI, "annotation array initialization rcurly",
                8, "12, 16"),
            "43:13: " + getCheckMessage(MSG_CHILD_ERROR, "annotation array initialization",
                12, 16),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidTryWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidTryIndent.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidTryWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidTryIndent.java");
        final String[] expected = {
            "25:10: " + getCheckMessage(MSG_ERROR, "try", 9, 8),
            "26:8: " + getCheckMessage(MSG_ERROR, "try rcurly", 7, 8),
            "28:8: " + getCheckMessage(MSG_ERROR, "catch rcurly", 7, 8),
            "30:5: " + getCheckMessage(MSG_ERROR, "try", 4, 8),
            "31:9: " + getCheckMessage(MSG_CHILD_ERROR, "try", 8, 12),
            "32:5: " + getCheckMessage(MSG_ERROR, "try rcurly", 4, 8),
            "33:9: " + getCheckMessage(MSG_CHILD_ERROR, "finally", 8, 12),
            "38:9: " + getCheckMessage(MSG_CHILD_ERROR, "catch", 8, 12),
            "43:11: " + getCheckMessage(MSG_ERROR, "try rcurly", 10, 8),
            "45:7: " + getCheckMessage(MSG_ERROR, "catch rcurly", 6, 8),
            "52:6: " + getCheckMessage(MSG_ERROR, "catch rcurly", 5, 8),
            "59:11: " + getCheckMessage(MSG_CHILD_ERROR, "catch", 10, 12),
            "60:15: " + getCheckMessage(MSG_CHILD_ERROR, "catch", 14, 12),
            "61:11: " + getCheckMessage(MSG_CHILD_ERROR, "catch", 10, 12),
            "63:7: " + getCheckMessage(MSG_ERROR, "catch", 6, 8),
            "70:11: " + getCheckMessage(MSG_ERROR, "try lcurly", 10, 8),
            "72:11: " + getCheckMessage(MSG_ERROR, "try rcurly", 10, 8),
            "74:7: " + getCheckMessage(MSG_ERROR, "catch lcurly", 6, 8),
            "77:11: " + getCheckMessage(MSG_ERROR, "catch rcurly", 10, 8),
            "80:11: " + getCheckMessage(MSG_CHILD_ERROR, "catch", 10, 12),
            "86:1: " + getCheckMessage(MSG_ERROR, "try", 0, 8),
            "87:1: " + getCheckMessage(MSG_ERROR, "try rcurly", 0, 8),
            "88:1: " + getCheckMessage(MSG_CHILD_ERROR, "catch", 0, 12),
            "89:1: " + getCheckMessage(MSG_ERROR, "catch rcurly", 0, 8),
            "91:1: " + getCheckMessage(MSG_ERROR, "try", 0, 8),
            "92:1: " + getCheckMessage(MSG_ERROR, "try rcurly", 0, 8),
            "93:1: " + getCheckMessage(MSG_CHILD_ERROR, "catch", 0, 12),
            "94:1: " + getCheckMessage(MSG_ERROR, "catch rcurly", 0, 8),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testCatchParametersOnNewLine()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "2");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "2");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName =
            getPath("InputIndentationCatchParametersOnNewLine.java");

        final String[] expected = {
            "22:1: " + getCheckMessage(MSG_CHILD_ERROR, "catch", 0, 8),
            "31:5: " + getCheckMessage(MSG_CHILD_ERROR, "catch", 4, 8),
            "38:5: " + getCheckMessage(MSG_CHILD_ERROR, "catch", 4, 8),
            "48:13: " + getCheckMessage(MSG_CHILD_ERROR, "catch", 12, 8),
            "64:9: " + getCheckMessage(MSG_CHILD_ERROR, "catch", 8, 12),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testMultiLineStatements()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "2");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "2");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName =
            getPath("InputIndentationMultilineStatements.java");

        final String[] expected = {
            "23:7: " + getCheckMessage(MSG_CHILD_ERROR, "method def", 6, 8),
            "39:7: " + getCheckMessage(MSG_ERROR, 0, 6, 8),
            "40:7: " + getCheckMessage(MSG_ERROR, 1, 6, 8),
            "65:7: " + getCheckMessage(MSG_CHILD_ERROR, "method def", 6, 8),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidClassDefWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidClassDefIndent.java");
        final String[] expected = {
            "18:3: " + getCheckMessage(MSG_ERROR, "class def modifier", 2, 0),
            "24:3: " + getCheckMessage(MSG_ERROR, "class def lcurly", 2, 0),
            "27:3: " + getCheckMessage(MSG_ERROR, "class def rcurly", 2, 0),
            "30:9: " + getCheckMessage(MSG_ERROR, "class def ident", 2, 0),
            "34:3: " + getCheckMessage(MSG_ERROR, "class def rcurly", 2, 0),
            "39:3: " + getCheckMessage(MSG_ERROR, "extends", 2, 4),
            "40:3: " + getCheckMessage(MSG_ERROR, "implements", 2, 4),
            "46:3: " + getCheckMessage(MSG_ERROR, "extends", 2, 4),
            "54:3: " + getCheckMessage(MSG_ERROR, "implements", 2, 4),
            "55:3: " + getCheckMessage(MSG_ERROR, "java", 2, 4),
            "60:3: " + getCheckMessage(MSG_ERROR, "class def modifier", 2, 0),
            "61:3: " + getCheckMessage(MSG_ERROR, "class def lcurly", 2, 0),
            "69:3: " + getCheckMessage(MSG_ERROR, "class def rcurly", 2, 0),
            "73:3: " + getCheckMessage(MSG_ERROR, "extends", 2, 4),
            "79:1: " + getCheckMessage(MSG_ERROR, "class", 0, 4),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidClassDefWithChecker1()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidClassDefIndent1.java");
        final String[] expected = {
            "22:3: " + getCheckMessage(MSG_ERROR, "class def modifier", 2, 0),
            "27:9: " + getCheckMessage(MSG_ERROR, "class def ident", 2, 4),
            "29:13: " + getCheckMessage(MSG_ERROR, "class def ident", 6, 4),
            "31:9: " + getCheckMessage(MSG_ERROR, "class def ident", 2, 4),
            "34:7: " + getCheckMessage(MSG_ERROR, "member def modifier", 6, 8),
            "40:11: " + getCheckMessage(MSG_ERROR, "int", 10, 12),
            "44:7: " + getCheckMessage(MSG_ERROR, "member def modifier", 6, 8),
            "49:7: " + getCheckMessage(MSG_ERROR, "class def rcurly", 6, 4),
            "51:13: " + getCheckMessage(MSG_ERROR, "class def ident", 6, 4),
            "56:13: " + getCheckMessage(MSG_ERROR, "class def ident", 6, 8),
            "59:17: " + getCheckMessage(MSG_ERROR, "class def ident", 10, 8),
            "61:11: " + getCheckMessage(MSG_ERROR, "class def rcurly", 10, 8),
            "64:11: " + getCheckMessage(MSG_ERROR, "member def type", 10, 12),
            "69:11: " + getCheckMessage(MSG_CHILD_ERROR, "method def", 10, 8),
            "70:9: " + getCheckMessage(MSG_ERROR_MULTI, "object def lcurly", 8, "10, 14"),
            "74:9: " + getCheckMessage(MSG_ERROR_MULTI, "object def rcurly", 8, "10, 14"),
            "77:7: " + getCheckMessage(MSG_ERROR_MULTI, "object def lcurly", 6, "8, 12"),
            "81:7: " + getCheckMessage(MSG_ERROR_MULTI, "object def rcurly", 6, "8, 12"),
            "85:11: " + getCheckMessage(MSG_ERROR, "method def modifier", 10, 12),
            "87:11: " + getCheckMessage(MSG_ERROR, "method def rcurly", 10, 12),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidBlockWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidBlockIndent.java");
        final String[] expected = {
            "26:8: " + getCheckMessage(MSG_ERROR, "block lcurly", 7, 8),
            "27:10: " + getCheckMessage(MSG_ERROR, "block lcurly", 9, 8),
            "29:10: " + getCheckMessage(MSG_ERROR, "block lcurly", 9, 8),
            "30:8: " + getCheckMessage(MSG_ERROR, "block rcurly", 7, 8),
            "32:7: " + getCheckMessage(MSG_ERROR, "block lcurly", 6, 8),
            "34:7: " + getCheckMessage(MSG_ERROR, "block rcurly", 6, 8),
            "35:7: " + getCheckMessage(MSG_ERROR, "block lcurly", 6, 8),
            "38:10: " + getCheckMessage(MSG_ERROR, "block lcurly", 9, 8),
            "39:14: " + getCheckMessage(MSG_CHILD_ERROR, "block", 13, 12),
            "41:14: " + getCheckMessage(MSG_CHILD_ERROR, "block", 13, 12),
            "42:10: " + getCheckMessage(MSG_ERROR, "block rcurly", 9, 8),
            "45:7: " + getCheckMessage(MSG_ERROR, "block lcurly", 6, 8),
            "46:11: " + getCheckMessage(MSG_CHILD_ERROR, "block", 10, 12),
            "48:11: " + getCheckMessage(MSG_CHILD_ERROR, "block", 10, 12),
            "49:7: " + getCheckMessage(MSG_ERROR, "block rcurly", 6, 8),
            "52:7: " + getCheckMessage(MSG_ERROR, "block lcurly", 6, 8),
            "55:11: " + getCheckMessage(MSG_CHILD_ERROR, "block", 10, 12),
            "59:11: " + getCheckMessage(MSG_ERROR, "block lcurly", 10, 12),
            "63:11: " + getCheckMessage(MSG_ERROR, "block rcurly", 10, 12),
            "68:11: " + getCheckMessage(MSG_CHILD_ERROR, "block", 10, 12),
            "70:11: " + getCheckMessage(MSG_ERROR, "block lcurly", 10, 12),
            "71:15: " + getCheckMessage(MSG_CHILD_ERROR, "block", 14, 16),
            "86:11: " + getCheckMessage(MSG_ERROR, "block rcurly", 10, 12),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidBlockWithChecker1()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidBlockIndent1.java");
        final String[] expected = {
            "27:3: " + getCheckMessage(MSG_ERROR, "static initialization", 2, 4),
            "28:7: " + getCheckMessage(MSG_ERROR, "static initialization", 6, 4),
            "32:8: " + getCheckMessage(MSG_CHILD_ERROR, "static initialization", 7, 8),
            "35:7: " + getCheckMessage(MSG_ERROR, "static initialization", 6, 4),
            "37:3: " + getCheckMessage(MSG_ERROR, "static initialization rcurly", 2, 4),
            "39:3: " + getCheckMessage(MSG_ERROR, "static initialization", 2, 4),
            "41:7: " + getCheckMessage(MSG_ERROR, "static initialization rcurly", 6, 4),
            "43:3: " + getCheckMessage(MSG_ERROR, "static initialization", 2, 4),
            "45:7: " + getCheckMessage(MSG_CHILD_ERROR, "static initialization", 6, 8),
            "48:3: " + getCheckMessage(MSG_ERROR, "static initialization lcurly", 2, 4),
            "49:7: " + getCheckMessage(MSG_CHILD_ERROR, "static initialization", 6, 8),
            "50:7: " + getCheckMessage(MSG_ERROR, "static initialization rcurly", 6, 4),
            "55:7: " + getCheckMessage(MSG_CHILD_ERROR, "static initialization", 6, 8),
            "60:5: " + getCheckMessage(MSG_CHILD_ERROR, "static initialization", 4, 8),
            "61:3: " + getCheckMessage(MSG_ERROR, "static initialization rcurly", 2, 4),
            "66:7: " + getCheckMessage(MSG_ERROR, "static initialization rcurly", 6, 4),
            "69:3: " + getCheckMessage(MSG_ERROR, "block lcurly", 2, 4),
            "70:7: " + getCheckMessage(MSG_ERROR, "block lcurly", 6, 4),
            "73:3: " + getCheckMessage(MSG_ERROR, "block lcurly", 2, 4),
            "75:7: " + getCheckMessage(MSG_ERROR, "block rcurly", 6, 4),
            "77:7: " + getCheckMessage(MSG_ERROR, "block lcurly", 6, 4),
            "79:3: " + getCheckMessage(MSG_ERROR, "block rcurly", 2, 4),
            "82:7: " + getCheckMessage(MSG_CHILD_ERROR, "block", 6, 8),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidIfWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidIfIndent.java");
        final String[] expected = {
            "55:2: " + getCheckMessage(MSG_ERROR, "if", 1, 8),
            "60:10: " + getCheckMessage(MSG_ERROR, "if", 9, 8),
            "61:10: " + getCheckMessage(MSG_ERROR, "if lcurly", 9, 8),
            "62:8: " + getCheckMessage(MSG_ERROR, "if rcurly", 7, 8),
            "64:7: " + getCheckMessage(MSG_ERROR, "if", 6, 8),
            "65:6: " + getCheckMessage(MSG_ERROR, "if lcurly", 5, 8),
            "66:6: " + getCheckMessage(MSG_ERROR, "if rcurly", 5, 8),
            "70:11: " + getCheckMessage(MSG_ERROR, "if rcurly", 10, 8),
            "71:8: " + getCheckMessage(MSG_ERROR, "else rcurly", 7, 8),
            "74:10: " + getCheckMessage(MSG_ERROR, "if", 9, 8),

            "75:8: " + getCheckMessage(MSG_ERROR, "if lcurly", 7, 8),
            "77:10: " + getCheckMessage(MSG_ERROR, "else", 9, 8),
            "79:10: " + getCheckMessage(MSG_ERROR, "else rcurly", 9, 8),
            "82:11: " + getCheckMessage(MSG_ERROR, "if", 10, 8),
            "83:8: " + getCheckMessage(MSG_ERROR, "if rcurly", 7, 8),
            "84:10: " + getCheckMessage(MSG_ERROR, "else", 9, 8),
            "85:8: " + getCheckMessage(MSG_ERROR, "else lcurly", 7, 8),
            "86:10: " + getCheckMessage(MSG_ERROR, "else rcurly", 9, 8),

            "90:10: " + getCheckMessage(MSG_ERROR, "if", 9, 8),
            "91:10: " + getCheckMessage(MSG_ERROR, "if lcurly", 9, 8),
            "92:10: " + getCheckMessage(MSG_ERROR, "if rcurly", 9, 8),
            "93:8: " + getCheckMessage(MSG_ERROR, "else lcurly", 7, 8),
            "94:11: " + getCheckMessage(MSG_ERROR, "else rcurly", 10, 8),
            "97:7: " + getCheckMessage(MSG_ERROR, "if", 6, 8),
            "98:11: " + getCheckMessage(MSG_ERROR, "if lcurly", 10, 8),
            "99:11: " + getCheckMessage(MSG_ERROR, "if rcurly", 10, 8),
            "100:8: " + getCheckMessage(MSG_ERROR, "else rcurly", 7, 8),
            "103:6: " + getCheckMessage(MSG_ERROR, "if", 5, 8),
            "104:12: " + getCheckMessage(MSG_ERROR, "if rcurly", 11, 8),
            "105:6: " + getCheckMessage(MSG_ERROR, "else", 5, 8),
            "106:12: " + getCheckMessage(MSG_ERROR, "else rcurly", 11, 8),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidIfWithChecker1()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidIfIndent1.java");
        final String[] expected = {
            "37:15: " + getCheckMessage(MSG_CHILD_ERROR, "if", 14, 12),
            "42:11: " + getCheckMessage(MSG_ERROR, "if lcurly", 10, 8),
            "43:11: " + getCheckMessage(MSG_CHILD_ERROR, "if", 10, 12),
            "48:15: " + getCheckMessage(MSG_CHILD_ERROR, "if", 14, 12),
            "49:11: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 10, 12),
            "51:11: " + getCheckMessage(MSG_CHILD_ERROR, "else", 10, 12),
            "52:9: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 8, 12),

            "59:17: " + getCheckMessage(MSG_CHILD_ERROR, "if", 16, 12),
            "60:10: " + getCheckMessage(MSG_ERROR, "if rcurly", 9, 8),
            "63:17: " + getCheckMessage(MSG_CHILD_ERROR, "else", 16, 12),
            "69:1: " + getCheckMessage(MSG_CHILD_ERROR, "if", 0, 12),
            "73:41: " + getCheckMessage(MSG_CHILD_ERROR, "else", 40, 12),
            "80:15: " + getCheckMessage(MSG_CHILD_ERROR, "if", 14, 12),

            "83:15: " + getCheckMessage(MSG_CHILD_ERROR, "else", 14, 12),
            "89:11: " + getCheckMessage(MSG_CHILD_ERROR, "if", 10, 12),
            "91:11: " + getCheckMessage(MSG_CHILD_ERROR, "else", 10, 12),
            "95:11: " + getCheckMessage(MSG_ERROR, "if", 10, 8),
            "96:15: " + getCheckMessage(MSG_CHILD_ERROR, "if", 14, 12),
            "97:11: " + getCheckMessage(MSG_ERROR, "if rcurly", 10, 8),
            "98:11: " + getCheckMessage(MSG_ERROR, "else", 10, 8),
            "99:15: " + getCheckMessage(MSG_CHILD_ERROR, "else", 14, 12),
            "100:11: " + getCheckMessage(MSG_ERROR, "else rcurly", 10, 8),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidIfWithChecker2()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidIfIndent2.java");
        final String[] expected = {
            "26:10: " + getCheckMessage(MSG_CHILD_ERROR, "if", 9, 12),
            "27:12: " + getCheckMessage(MSG_CHILD_ERROR, "if", 11, 12),
            "31:11: " + getCheckMessage(MSG_CHILD_ERROR, "if", 10, 12),
            "34:8: " + getCheckMessage(MSG_ERROR, "if rcurly", 7, 8),
            "41:11: " + getCheckMessage(MSG_CHILD_ERROR, "if", 10, 12),
            "43:11: " + getCheckMessage(MSG_CHILD_ERROR, "if", 10, 12),

            "50:11: " + getCheckMessage(MSG_CHILD_ERROR, "if", 10, 12),
            "59:11: " + getCheckMessage(MSG_ERROR, "if", 10, 12),
            "63:19: " + getCheckMessage(MSG_CHILD_ERROR, "if", 18, 20),
            "74:11: " + getCheckMessage(MSG_ERROR, "if rparen", 10, 8),
            "79:7: " + getCheckMessage(MSG_ERROR, "if rparen", 6, 8),
            "85:7: " + getCheckMessage(MSG_ERROR, "if lparen", 6, 8),
            "87:7: " + getCheckMessage(MSG_ERROR, "if rparen", 6, 8),
            "90:1: " + getCheckMessage(MSG_ERROR, "if", 0, 8),
            "91:1: " + getCheckMessage(MSG_CHILD_ERROR, "if", 0, 12),
            "92:1: " + getCheckMessage(MSG_CHILD_ERROR, "if", 0, 12),
            "93:1: " + getCheckMessage(MSG_ERROR, "if rcurly", 0, 8),
            "94:1: " + getCheckMessage(MSG_ERROR, "if", 0, 8),
            "95:1: " + getCheckMessage(MSG_CHILD_ERROR, "if", 0, 12),
            "96:1: " + getCheckMessage(MSG_ERROR, "else", 0, 8),
            "97:1: " + getCheckMessage(MSG_CHILD_ERROR, "else", 0, 12),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidWhileWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidWhileIndent.java");
        final String[] expected = {
            "25:10: " + getCheckMessage(MSG_ERROR, "while", 9, 8),
            "26:8: " + getCheckMessage(MSG_ERROR, "while rcurly", 7, 8),
            "28:8: " + getCheckMessage(MSG_ERROR, "while", 7, 8),
            "29:10: " + getCheckMessage(MSG_ERROR, "while lcurly", 9, 8),
            "30:10: " + getCheckMessage(MSG_ERROR, "while rcurly", 9, 8),

            "32:10: " + getCheckMessage(MSG_ERROR, "while", 9, 8),
            "33:7: " + getCheckMessage(MSG_ERROR, "while lcurly", 6, 8),
            "34:15: " + getCheckMessage(MSG_CHILD_ERROR, "while", 14, 12),
            "35:7: " + getCheckMessage(MSG_ERROR, "while rcurly", 6, 8),

            "37:11: " + getCheckMessage(MSG_ERROR, "while", 10, 8),
            "39:11: " + getCheckMessage(MSG_ERROR, "while rcurly", 10, 8),
            "41:11: " + getCheckMessage(MSG_ERROR, "while", 10, 8),
            "44:11: " + getCheckMessage(MSG_ERROR, "while rcurly", 10, 8),

            "46:7: " + getCheckMessage(MSG_ERROR, "while", 6, 8),
            "47:11: " + getCheckMessage(MSG_ERROR, "while lcurly", 10, 8),
            "50:7: " + getCheckMessage(MSG_ERROR, "while rcurly", 6, 8),
            "53:15: " + getCheckMessage(MSG_ERROR, "if", 14, 12),
            "54:19: " + getCheckMessage(MSG_CHILD_ERROR, "if", 18, 16),
            "55:15: " + getCheckMessage(MSG_ERROR, "if rcurly", 14, 12),
            "56:15: " + getCheckMessage(MSG_CHILD_ERROR, "while", 14, 12),
            "57:11: " + getCheckMessage(MSG_ERROR, "while rcurly", 10, 8),

            "60:11: " + getCheckMessage(MSG_CHILD_ERROR, "while", 10, 12),
            "66:11: " + getCheckMessage(MSG_CHILD_ERROR, "while", 10, 12),
            "71:11: " + getCheckMessage(MSG_CHILD_ERROR, "while", 10, 12),
            "78:6: " + getCheckMessage(MSG_ERROR, "while rparen", 5, 8),
            "85:11: " + getCheckMessage(MSG_ERROR, "while rparen", 10, 8),
            "92:11: " + getCheckMessage(MSG_ERROR, "while rparen", 10, 8),
            "99:9: " + getCheckMessage(MSG_CHILD_ERROR, "while", 8, 12),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidInvalidAnonymousClass() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidAnonymousClassIndent.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidForWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidForIndent.java");
        final String[] expected = {
            "26:7: " + getCheckMessage(MSG_ERROR, "for", 6, 8),
            "27:11: " + getCheckMessage(MSG_ERROR, "for rcurly", 10, 8),
            "29:10: " + getCheckMessage(MSG_ERROR, "for", 9, 8),
            "30:7: " + getCheckMessage(MSG_ERROR, "for lcurly", 6, 8),
            "31:7: " + getCheckMessage(MSG_ERROR, "for rcurly", 6, 8),
            "35:11: " + getCheckMessage(MSG_CHILD_ERROR, "for", 10, 12),

            "36:11: " + getCheckMessage(MSG_ERROR, "for rcurly", 10, 8),
            "39:11: " + getCheckMessage(MSG_ERROR, "for lcurly", 10, 8),
            "40:11: " + getCheckMessage(MSG_CHILD_ERROR, "for", 10, 12),
            "48:11: " + getCheckMessage(MSG_CHILD_ERROR, "for", 10, 12),
            "54:8: " + getCheckMessage(MSG_ERROR, "for", 7, 8),

            "55:11: " + getCheckMessage(MSG_CHILD_ERROR, "for", 10, 12),
            "64:8: " + getCheckMessage(MSG_CHILD_ERROR, "for", 7, 12),

            "69:7: " + getCheckMessage(MSG_ERROR, "for", 6, 8),
            "70:11: " + getCheckMessage(MSG_CHILD_ERROR, "for", 10, 12),
            "71:15: " + getCheckMessage(MSG_CHILD_ERROR, "for", 14, 16),
            "72:11: " + getCheckMessage(MSG_CHILD_ERROR, "for", 10, 12),
            "81:13: " + getCheckMessage(MSG_ERROR, "for rparen", 12, 8),
            "86:3: " + getCheckMessage(MSG_ERROR, "method def modifier", 2, 4),
            "87:5: " + getCheckMessage(MSG_ERROR, "for", 4, 8),
            "88:9: " + getCheckMessage(MSG_CHILD_ERROR, "for", 8, 12),
            "89:7: " + getCheckMessage(MSG_CHILD_ERROR, "for", 6, 12),
            "90:9: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 8, 16),
            "92:1: " + getCheckMessage(MSG_ERROR, "for", 0, 8),
            "93:1: " + getCheckMessage(MSG_ERROR, "for lparen", 0, 8),
            "94:1: " + getCheckMessage(MSG_CHILD_ERROR, "for", 0, 12),
            "95:1: " + getCheckMessage(MSG_ERROR, ";", 0, 4),
            "96:1: " + getCheckMessage(MSG_CHILD_ERROR, "for", 0, 12),
            "97:1: " + getCheckMessage(MSG_ERROR, ";", 0, 4),
            "98:1: " + getCheckMessage(MSG_CHILD_ERROR, "for", 0, 12),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidForWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidForIndent.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidDoWhileWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidDoWhileIndent.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInvalidDoWhileWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationInvalidDoWhileIndent.java");
        final String[] expected = {
            "7:1: " + getCheckMessage(MSG_ERROR, "do..while", 0, 8),
            "8:1: " + getCheckMessage(MSG_ERROR, "do..while", 0, 8),
            "9:1: " + getCheckMessage(MSG_ERROR, "do..while", 0, 8),
            "10:1: " + getCheckMessage(MSG_ERROR, "do..while rcurly", 0, 8),
            "11:1: " + getCheckMessage(MSG_ERROR, "do..while", 0, 8),
            "12:1: " + getCheckMessage(MSG_ERROR, "do..while while", 0, 8),
            "13:1: " + getCheckMessage(MSG_ERROR, "do..while", 0, 8),
            "14:1: " + getCheckMessage(MSG_ERROR, "do..while lcurly", 0, 8),
            "15:1: " + getCheckMessage(MSG_ERROR, "do..while", 0, 8),
            "16:1: " + getCheckMessage(MSG_ERROR, "do..while while", 0, 8),
            "17:1: " + getCheckMessage(MSG_ERROR, "do..while lparen", 0, 8),
            "18:1: " + getCheckMessage(MSG_ERROR, "do..while", 0, 8),
            "19:1: " + getCheckMessage(MSG_ERROR, "do..while lparen", 0, 8),
            "20:1: " + getCheckMessage(MSG_ERROR, "do..while", 0, 8),
            "21:1: " + getCheckMessage(MSG_ERROR, "do..while lparen", 0, 8),
            "22:1: " + getCheckMessage(MSG_CHILD_ERROR, "do..while", 0, 8),
            "23:1: " + getCheckMessage(MSG_ERROR, "do..while rparen", 0, 8),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidBlockWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidBlockIndent.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidBlockWithChecker1()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidBlockIndent1.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidWhileWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidWhileIndent.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidClassDefWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidClassDefIndent.java");
        final String[] expected = {
            "38:9: " + getCheckMessage(MSG_ERROR, "int", 8, 12),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidClassDefWithChecker1()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidClassDefIndent1.java");
        final String[] expected = {
            "43:1: " + getCheckMessage(MSG_ERROR, "class", 0, 4),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidInterfaceDefWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidInterfaceDefIndent.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testValidCommaWithChecker()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String fileName = getPath("InputIndentationValidCommaIndent.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testTabs() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = {
            "29:10: " + getCheckMessage(MSG_CHILD_ERROR, "ctor def", 9, 8),
        };
        verifyWarns(checkConfig, getPath("InputIndentationUseTabs.java"), expected);
    }

    @Test
    public void testIndentationLevel() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "2");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = {
            "29:6: " + getCheckMessage(MSG_CHILD_ERROR, "ctor def", 5, 4),
        };
        verifyWarns(checkConfig, getPath("InputIndentationUseTwoSpaces.java"), expected);
    }

    @Test
    public void testThrowsIndentationLevel() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "8");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationInvalidThrowsIndent.java"), expected);
    }

    @Test
    public void testThrowsIndentationLevel2() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("basicOffset", "1");
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("lineWrappingIndentation", "3");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "5");
        final String[] expected = {
            "7:1: " + getCheckMessage(MSG_ERROR, "Exception", 0, 6),
            "10:1: " + getCheckMessage(MSG_ERROR, "NullPointerException", 0, 6),
            "13:1: " + getCheckMessage(MSG_ERROR, "throws", 0, 6),
            "16:1: " + getCheckMessage(MSG_ERROR, "throws", 0, 6),
            "18:1: " + getCheckMessage(MSG_ERROR, "throws", 0, 6),
            "19:1: " + getCheckMessage(MSG_ERROR, "Exception", 0, 6),
            "22:1: " + getCheckMessage(MSG_ERROR, "throws", 0, 6),
            "23:1: " + getCheckMessage(MSG_ERROR, "Exception", 0, 6),
            "24:1: " + getCheckMessage(MSG_ERROR, "NullPointerException", 0, 6),
            "27:1: " + getCheckMessage(MSG_ERROR, "throws", 0, 6),
            "28:1: " + getCheckMessage(MSG_ERROR, "Exception", 0, 6),
            "31:1: " + getCheckMessage(MSG_ERROR, "throws", 0, 6),
            "37:1: " + getCheckMessage(MSG_ERROR, "throws", 0, 6),
        };
        verifyWarns(checkConfig, getPath("InputIndentationInvalidThrowsIndent2.java"), expected);
    }

    @Test
    public void testCaseLevel() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "0");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = {
            "27:11: " + getCheckMessage(MSG_CHILD_ERROR, "case", 10, 8),
        };
        verifyWarns(checkConfig, getPath("InputIndentationCaseLevel.java"), expected);
    }

    @Test
    public void testBraceAdjustment() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "2");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = {
            "24:9: " + getCheckMessage(MSG_CHILD_ERROR, "ctor def", 8, 10),
            "25:9: " + getCheckMessage(MSG_ERROR, "if", 8, 10),
            "26:11: " + getCheckMessage(MSG_ERROR, "if lcurly", 10, 12),
            "27:13: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "if", 12, "14, 16"),
            "28:9: " + getCheckMessage(MSG_ERROR, "if rcurly", 8, 12),
        };
        verifyWarns(checkConfig, getPath("InputIndentationBraceAdjustment.java"), expected);
    }

    @Test
    public void testInvalidAssignWithChecker() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = {
            "22:11: " + getCheckMessage(MSG_ERROR, "getLineNo", 10, 12),
            "24:11: " + getCheckMessage(MSG_ERROR, "getLine", 10, 12),
            "28:10: " + getCheckMessage(MSG_ERROR, "=", 9, 12),
            "29:11: " + getCheckMessage(MSG_ERROR, "1", 10, 12),
        };
        verifyWarns(checkConfig, getPath("InputIndentationInvalidAssignIndent.java"), expected);
    }

    @Test
    public void testInvalidImportIndent() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("basicOffset", "8");
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = {
            "4:3: " + getCheckMessage(MSG_ERROR, ".", 2, 4),
            "5:2: " + getCheckMessage(MSG_ERROR, "import", 1, 0),
        };
        verifyWarns(checkConfig, getPath("InputIndentationInvalidImportIndent.java"), expected);
    }

    @Test
    public void testValidAssignWithChecker() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationValidAssignIndent.java"), expected);
    }

    @Test
    public void test15Extensions() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentation15Extensions.java"), expected);
    }

    @Test
    public void testTryResources() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationValidTryResourcesIndent.java"),
               expected);
    }

    @Test
    public void testSwitchCustom() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "8");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationSwitchCustom.java"),
               expected);
    }

    @Test
    public void testSynchronizedStatement() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "8");
        final String[] expected = {
            "27:1: " + getCheckMessage(MSG_CHILD_ERROR, "synchronized", 0, 12),
            "30:13: " + getCheckMessage(MSG_ERROR, "synchronized lparen", 12, 8),
        };
        verifyWarns(checkConfig, getPath("InputIndentationSynchronizedStatement.java"), expected);
    }

    @Test
    public void testSynchronizedMethod() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("throwsIndent", "8");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationSynchronizedMethod.java"), expected);
    }

    @Test
    public void testAnonymousClassInMethod() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "8");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("throwsIndent", "4");
        checkConfig.addProperty("arrayInitIndent", "2");
        final String[] expected = {
            "19:9: " + getCheckMessage(MSG_ERROR, "method def modifier", 8, 2),
            "20:17: " + getCheckMessage(MSG_CHILD_ERROR, "method def", 16, 4),
            "21:25: " + getCheckMessage(MSG_ERROR_MULTI, "method def modifier", 24, "18, 20, 22"),
            "23:33: " + getCheckMessage(MSG_CHILD_ERROR_MULTI, "method def", 32, "20, 22, 24"),
            "24:25: " + getCheckMessage(MSG_ERROR_MULTI, "method def rcurly", 24, "18, 20, 22"),
            "26:9: " + getCheckMessage(MSG_ERROR, "method def rcurly", 8, 2),
        };
        verifyWarns(checkConfig, getPath("InputIndentationAnonymousClassInMethod.java"), expected);
    }

    @Test
    public void testAnonymousClassInMethodWithCurlyOnNewLine() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        checkConfig.addProperty("throwsIndent", "4");
        checkConfig.addProperty("arrayInitIndent", "4");
        final String[] expected = {
            "38:19: " + getCheckMessage(MSG_ERROR_MULTI, "object def rcurly", 18, "16, 20, 24"),
            "40:15: " + getCheckMessage(MSG_ERROR, "new", 14, 16),
            "46:15: " + getCheckMessage(MSG_ERROR_MULTI, "object def rcurly", 14, "16, 20, 24"),
            "58:19: " + getCheckMessage(MSG_ERROR_MULTI, "object def lcurly", 18, "16, 20, 24"),
            "64:19: " + getCheckMessage(MSG_ERROR_MULTI, "object def rcurly", 18, "16, 20, 24"),
            "67:15: " + getCheckMessage(MSG_ERROR_MULTI, "object def lcurly", 14, "16, 20, 24"),
            "73:15: " + getCheckMessage(MSG_ERROR_MULTI, "object def rcurly", 14, "16, 20, 24"),
        };
        verifyWarns(checkConfig,
            getPath("InputIndentationAnonymousClassInMethodCurlyOnNewLine.java"), expected);
    }

    @Test
    public void testAnnotationDefinition() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationAnnotationDefinition.java"), expected);
    }

    @Test
    public void testPackageDeclaration() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = {
            "1:2: " + getCheckMessage(MSG_ERROR, "package def", 1, 0),
        };
        verifyWarns(checkConfig, getPath("InputIndentationPackageDeclaration.java"), expected);
    }

    @Test
    public void testPackageDeclaration2() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = {
            "2:2: " + getCheckMessage(MSG_ERROR, "package def", 1, 0),
        };
        verifyWarns(checkConfig,
            getPath("package-info.java"), expected);
    }

    @Test
    public void testPackageDeclaration3() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationPackageDeclaration3.java"), expected);
    }

    @Test
    public void testPackageDeclaration4() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = {
            "2:1: " + getCheckMessage(MSG_ERROR, "com", 0, 4),
            "3:1: " + getCheckMessage(MSG_ERROR, "checks", 0, 4),
        };
        verifyWarns(checkConfig, getPath("InputIndentationPackageDeclaration4.java"), expected);
    }

    @Test
    public void testLambda() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "2");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        final String[] expected = {
            "37:6: " + getCheckMessage(MSG_ERROR_MULTI, "block lcurly", 5, "4, 8"),
            "38:6: " + getCheckMessage(MSG_ERROR_MULTI, "block rcurly", 5, "4, 8"),
            "42:12: " + getCheckMessage(MSG_ERROR, "lambda", 11, 12),
            "43:10: " + getCheckMessage(MSG_ERROR, "block lcurly", 9, 8),
            "55:8: " + getCheckMessage(MSG_CHILD_ERROR, "block", 7, 6),
            "56:6: " + getCheckMessage(MSG_ERROR, "block rcurly", 5, 4),
        };
        verifyWarns(checkConfig, getPath("InputIndentationLambda.java"), expected);
    }

    @Test
    public void testLambda1() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "2");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        final String[] expected = {
            "68:10: " + getCheckMessage(MSG_CHILD_ERROR, "block", 9, 10),
            "69:12: " + getCheckMessage(MSG_CHILD_ERROR, "block", 11, 10),
            "74:8: " + getCheckMessage(MSG_ERROR, "block rcurly", 7, 8),
        };
        verifyWarns(checkConfig, getPath("InputIndentationLambda1.java"), expected);
    }

    @Test
    public void testLambda2() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationLambda2.java"), expected);
    }

    @Test
    public void testLambda3() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        final String[] expected = {
            "15:13: " + getCheckMessage(MSG_CHILD_ERROR, "method def", 12, 8),
            "29:13: " + getCheckMessage(MSG_CHILD_ERROR, "method def", 12, 8),
            "30:13: " + getCheckMessage(MSG_CHILD_ERROR, "block", 12, 16),
            "31:9: " + getCheckMessage(MSG_ERROR, "block rcurly", 8, 12),
            "65:13: " + getCheckMessage(MSG_CHILD_ERROR, "method def", 12, 8),
            "87:13: " + getCheckMessage(MSG_ERROR, "method def rcurly", 12, 8),
        };
        verifyWarns(checkConfig, getPath("InputIndentationLambda3.java"), expected);
    }

    @Test
    public void testLambda4() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationLambda4.java"), expected);
    }

    @Test
    public void testLambda5() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "3");
        checkConfig.addProperty("basicOffset", "3");
        checkConfig.addProperty("caseIndent", "0");
        checkConfig.addProperty("lineWrappingIndentation", "6");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationLambda5.java"), expected);
    }

    @Test
    public void testLambdaFalseForceStrictCondition() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "0");
        final String[] expected = {
            "34:5: " + getCheckMessage(MSG_ERROR, "(", 4, 8),
            "35:5: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 4, 12),
            "36:5: " + getCheckMessage(MSG_ERROR, "(", 4, 8),
            "47:5: " + getCheckMessage(MSG_ERROR_MULTI, "block rcurly", 4, "8, 16"),
            "73:5: " + getCheckMessage(MSG_ERROR, "->", 4, 8),
        };

        verifyWarns(checkConfig, getPath("InputIndentationLambda6.java"), expected);
    }

    @Test
    public void testLambdaTrueForceStrictCondition() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        final String[] expected = {
            "23:17: " + getCheckMessage(MSG_ERROR, "(", 16, 12),
            "24:17: " + getCheckMessage(MSG_ERROR, "->", 16, 12),
            "26:27: " + getCheckMessage(MSG_ERROR, "\"SECOND_ARG\"", 26, 12),
            "27:26: " + getCheckMessage(MSG_ERROR, "(", 25, 12),
            "30:17: " + getCheckMessage(MSG_ERROR, "(", 16, 12),
            "31:21: " + getCheckMessage(MSG_ERROR, "if", 20, 16),
            "32:25: " + getCheckMessage(MSG_CHILD_ERROR, "if", 24, 20),
            "33:21: " + getCheckMessage(MSG_ERROR, "if rcurly", 20, 16),
            "34:25: " + getCheckMessage(MSG_CHILD_ERROR, "else", 24, 20),
            "35:21: " + getCheckMessage(MSG_ERROR, "else rcurly", 20, 16),
            "36:17: " + getCheckMessage(MSG_ERROR, "block rcurly", 16, 12),
            "39:17: " + getCheckMessage(MSG_ERROR, "(", 16, 12),
            "40:17: " + getCheckMessage(MSG_ERROR, "->", 16, 12),
            "41:21: " + getCheckMessage(MSG_ERROR, "if", 20, 16),
            "44:1: " + getCheckMessage(MSG_ERROR, "block rcurly", 0, 12),
        };

        verifyWarns(checkConfig, getPath("InputIndentationLambda7.java"), expected);
    }

    @Test
    public void testLambdaOddConditions() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "3");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "7");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWarns(checkConfig, getPath("InputIndentationLambda8.java"), expected);
    }

    @Test
    public void testSeparatedStatements() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String fileName = getPath("InputIndentationSeparatedStatements.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testSeparatedLineWithJustSpaces() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String fileName = getPath("InputIndentationSeparatedStatementWithSpaces.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testTwoStatementsPerLine() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        final String fileName = getPath("InputIndentationTwoStatementsPerLine.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testMethodChaining() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        final String fileName = getPath("InputIndentationChainedMethods.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testMultipleAnnotationsWithWrappedLines() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        final String fileName =
            getPath("InputIndentationCorrectMultipleAnnotationsWithWrappedLines.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testMultipleAnnotationsWithWrappedLines1() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        final String fileName =
            getPath("InputIndentationCorrectMultipleAnnotationsWithWrappedLines1.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testMethodPrecedeByAnnotationsWithParameterOnSeparateLine() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("throwsIndent", "4");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("arrayInitIndent", "2");
        final String fileName =
            getPath("InputIndentationMethodPrecededByAnnotationWithParameterOnSeparateLine.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testAnnotationIncorrect() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        final String fileName =
            getPath("InputIndentationAnnotationIncorrect.java");
        final String[] expected = {
            "11:5: " + getCheckMessage(MSG_ERROR, "(", 4, 8),
            "14:9: " + getCheckMessage(MSG_ERROR, "(", 8, 12),
            "19:5: " + getCheckMessage(MSG_ERROR, "(", 4, 8),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInputAnnotationScopeIndentationCheck() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        final String fileName = getPath("InputIndentationAnnotationScopeIndentationCheck.java");
        final String[] expected = {
            "9:9: " + getCheckMessage(MSG_ERROR_MULTI,
                    "annotation array initialization rcurly", 8, "0, 4"),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInputAnnotationDefIndentationCheck() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        final String fileName = getPath("InputIndentationCustomAnnotation.java");
        final String[] expected = {
            "14:6: " + getCheckMessage(MSG_ERROR, "annotation def modifier", 5, 0),
            "15:6: " + getCheckMessage(MSG_ERROR, "annotation def rcurly", 5, 0),
            "16:6: " + getCheckMessage(MSG_ERROR, "@", 5, 0),
            "17:1: " + getCheckMessage(MSG_ERROR, "annotation field def modifier", 0, 4),
            "18:6: " + getCheckMessage(MSG_ERROR, "annotation def rcurly", 5, 0),
            "20:4: " + getCheckMessage(MSG_ERROR, "annotation def modifier", 3, 0),
            "22:1: " + getCheckMessage(MSG_ERROR, "annotation field def modifier", 0, 4),
            "23:6: " + getCheckMessage(MSG_ERROR, "annotation def rcurly", 5, 0),
            "25:6: " + getCheckMessage(MSG_ERROR, "@", 5, 0),
            "26:6: " + getCheckMessage(MSG_ERROR, "AnnotationWithLineWrap", 5, 0),
            "30:6: " + getCheckMessage(MSG_ERROR, "annotation def modifier", 5, 0),
            "31:4: " + getCheckMessage(MSG_ERROR, "annotation def modifier", 3, 0),
            "34:6: " + getCheckMessage(MSG_ERROR, "annotation field def modifier", 5, 4),
            "35:4: " + getCheckMessage(MSG_ERROR, "annotation def modifier", 3, 4),
            "36:1: " + getCheckMessage(MSG_ERROR, "annotation def modifier", 0, 4),
            "37:1: " + getCheckMessage(MSG_ERROR, "@", 0, 4),
            "38:9: " + getCheckMessage(MSG_ERROR, "AnnotationInnerLineWrap", 8, 4),
            "41:8: " + getCheckMessage(MSG_ERROR, "annotation field def modifier", 7, 8),
            "58:5: " + getCheckMessage(MSG_ERROR, "AnnotationInnerLineWrap2", 4, 0),
            "59:4: " + getCheckMessage(MSG_ERROR, "annotation field def modifier", 3, 4),
            "60:8: " + getCheckMessage(MSG_ERROR, "annotation field def modifier", 7, 4),
            "61:5: " + getCheckMessage(MSG_ERROR, "annotation def rcurly", 4, 0),
            "72:4: " + getCheckMessage(MSG_ERROR, "annotation def modifier", 3, 4),
            "87:29: " + getCheckMessage(MSG_ERROR_MULTI, "new", 28, "20, 24"),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInputAnnotationDefIndentationCheck1() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        final String fileName = getPath("InputIndentationCustomAnnotation1.java");
        final String[] expected = {
            "36:6: " + getCheckMessage(MSG_ERROR, "annotation def modifier", 5, 4),
            "47:2: " + getCheckMessage(MSG_ERROR, "interface", 1, 0),
            "53:12: " + getCheckMessage(MSG_ERROR, "@", 11, 0),
            "56:17: " + getCheckMessage(MSG_ERROR, "@", 16, 0),
            "63:13: " + getCheckMessage(MSG_ERROR, "@", 12, 4),
            "67:23: " + getCheckMessage(MSG_ERROR, "class def ident", 16, 0),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testTryResourcesStrict() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        final String fileName = getPath("InputIndentationTryWithResourcesStrict.java");
        final String[] expected = {
            "26:1: " + getCheckMessage(MSG_ERROR, "try resource", 0, 12),
            "28:14: " + getCheckMessage(MSG_ERROR_MULTI, "try rparen", 13, "8, 12"),
            "33:1: " + getCheckMessage(MSG_ERROR, "newBufferedWriter", 0, 16),
            "39:1: " + getCheckMessage(MSG_ERROR, "newBufferedWriter", 0, 12),
            "59:21: " + getCheckMessage(MSG_ERROR, "try resource", 20, 16),
            "79:14: " + getCheckMessage(MSG_ERROR, ".", 13, 12),
            "85:12: " + getCheckMessage(MSG_ERROR, ".", 11, 12),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testTryResourcesStrict1() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        final String fileName = getPath("InputIndentationTryWithResourcesStrict1.java");
        final String[] expected = {
            "31:20: " + getCheckMessage(MSG_ERROR, "writ", 19, 12),
            "38:20: " + getCheckMessage(MSG_ERROR, "writ", 19, 16),
            "45:22: " + getCheckMessage(MSG_ERROR, "writ", 21, 16),
            "60:18: " + getCheckMessage(MSG_ERROR, "zipFileName", 17, 16),
            "67:16: " + getCheckMessage(MSG_ERROR, "zipFileName", 15, 16),
            "77:8: " + getCheckMessage(MSG_ERROR, "try", 7, 8),
            "82:16: " + getCheckMessage(MSG_CHILD_ERROR, "try", 15, 12),
            "88:12: " + getCheckMessage(MSG_ERROR, "try resource", 11, 12),
            "89:10: " + getCheckMessage(MSG_CHILD_ERROR, "try", 9, 12),
            "93:12: " + getCheckMessage(MSG_ERROR, "try resource", 11, 12),
            "94:12: " + getCheckMessage(MSG_ERROR, "newBufferedWriter", 11, 16),
            "95:14: " + getCheckMessage(MSG_CHILD_ERROR, "try", 13, 12),
            "97:8: " + getCheckMessage(MSG_ERROR, "try", 7, 8),
            "98:8: " + getCheckMessage(MSG_ERROR_MULTI, "try rparen", 7, "8, 12"),
            "102:10: " + getCheckMessage(MSG_ERROR, "try", 9, 8),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testTryResourcesNotStrict() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        final String fileName = getPath("InputIndentationTryResourcesNotStrict.java");
        final String[] expected = {
            "26:1: " + getCheckMessage(MSG_ERROR, "try resource", 0, 12),
            "32:1: " + getCheckMessage(MSG_ERROR, "newBufferedWriter", 0, 16),
            "38:1: " + getCheckMessage(MSG_ERROR, "newBufferedWriter", 0, 12),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testTryResourcesNotStrict1() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        final String fileName = getPath("InputIndentationTryResourcesNotStrict1.java");
        final String[] expected = {
            "44:16: " + getCheckMessage(MSG_ERROR, "zipFileName", 15, 16),
            "54:8: " + getCheckMessage(MSG_ERROR, "try", 7, 8),
            "59:16: " + getCheckMessage(MSG_CHILD_ERROR, "try", 15, 12),
            "65:12: " + getCheckMessage(MSG_ERROR, "try resource", 11, 12),
            "66:10: " + getCheckMessage(MSG_CHILD_ERROR, "try", 9, 12),
            "70:12: " + getCheckMessage(MSG_ERROR, "try resource", 11, 12),
            "71:12: " + getCheckMessage(MSG_ERROR, "newBufferedWriter", 11, 16),
            "72:14: " + getCheckMessage(MSG_CHILD_ERROR, "try", 13, 12),
            "74:8: " + getCheckMessage(MSG_ERROR, "try", 7, 8),
            "75:8: " + getCheckMessage(MSG_ERROR_MULTI, "try rparen", 7, "8, 12"),
            "88:9: " + getCheckMessage(MSG_ERROR, ".", 8, 12),
            "96:12: " + getCheckMessage(MSG_ERROR, "new", 11, 12),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    /**
     * Verifies that the arguments of {@link IndentationCheck#MSG_ERROR},
     * {@link IndentationCheck#MSG_CHILD_ERROR}, {@link IndentationCheck#MSG_CHILD_ERROR_MULTI},
     * {@link IndentationCheck#MSG_CHILD_ERROR_MULTI} are in appropriate order.
     *
     * <p>In other tests, the argument 0 and text before it are chopped off and only the rest of
     * messages are verified. Therefore, the argument 0 is required to be the first argument in
     * the messages above. If we update the messages in the future, it is required to keep the
     * arguments in appropriate order to ensure other tests will work.</p>
     *
     * @see IndentComment#getExpectedMessagePostfix(String)
     */
    @Test
    public void testArgumentOrderOfErrorMessages() {
        final Object[] arguments = {"##0##", "##1##", "##2##"};
        final String[] messages = {
            getCheckMessage(MSG_ERROR, arguments),
            getCheckMessage(MSG_CHILD_ERROR, arguments),
            getCheckMessage(MSG_ERROR_MULTI, arguments),
            getCheckMessage(MSG_CHILD_ERROR_MULTI, arguments),
        };
        final boolean isInOrder = Arrays.stream(messages).allMatch(msg -> {
            final int indexOfArgumentZero = msg.indexOf((String) arguments[0]);
            return Arrays.stream(arguments)
                    .map(String.class::cast)
                    .mapToInt(msg::indexOf)
                    .allMatch(index -> index >= indexOfArgumentZero);
        });
        assertWithMessage(
                    "the argument 0 of error messages (indentation.error, indentation.child.error,"
                        + " indentation.error.multi, indentation.child.error.multi)"
                        + " is required to be the first argument of them")
                .that(isInOrder)
                .isTrue();
    }

    @Test
    public void testEmptyArray() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationEmptyArray.java"), expected);
    }

    @Test
    public void testNewHandler() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = {
            "10:1: " + getCheckMessage(MSG_ERROR, "Object", 0, 12),
            "12:1: " + getCheckMessage(MSG_ERROR, "(", 0, 12),
            "15:1: " + getCheckMessage(MSG_CHILD_ERROR, "new", 0, 8),
            "17:1: " + getCheckMessage(MSG_ERROR, "new lparen", 0, 8),
            "25:1: " + getCheckMessage(MSG_ERROR, "=", 0, 8),
        };
        verifyWarns(checkConfig, getPath("InputIndentationNewHandler.java"), expected);
    }

    @Test
    public void testTryHandler() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        checkConfig.addProperty("forceStrictCondition", "true");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, getPath("InputIndentationTryBlockWithResources.java"), expected);
    }

    @Test
    public void testTryHandler2() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        checkConfig.addProperty("forceStrictCondition", "true");
        final String[] expected = {
            "25:17: " + getCheckMessage(MSG_ERROR, "new", 16, 20),
            "27:13: " + getCheckMessage(MSG_ERROR, "new", 12, 20),
        };
        verifyWarns(checkConfig, getPath("InputIndentationTryBlock.java"), expected);
    }

    @Test
    public void testChainedMethodWithBracketOnNewLine() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);

        checkConfig.addProperty("arrayInitIndent", "2");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("forceStrictCondition", "false");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("tabWidth", "2");
        checkConfig.addProperty("throwsIndent", "2");
        final String[] expected = {
            "44:7: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 6, 8),
            "45:9: " + getCheckMessage(MSG_CHILD_ERROR, "method call", 8, 10),
            "47:7: " + getCheckMessage(MSG_ERROR, "method call rparen", 6, 8),
            "61:6: " + getCheckMessage(MSG_ERROR, "foo", 5, 8),
            "82:5: " + getCheckMessage(MSG_ERROR, "if rcurly", 4, 6),
            "84:3: " + getCheckMessage(MSG_CHILD_ERROR, "method def", 2, 4),
        };
        final String fileName = "InputIndentationChainedMethodWithBracketOnNewLine.java";
        verifyWarns(checkConfig, getPath(fileName), expected);
    }

    @Test
    public void testIndentationSwitchExpression() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = {
            "17:1: " + getCheckMessage(MSG_CHILD_ERROR, "case", 0, 12),
            "18:9: " + getCheckMessage(MSG_CHILD_ERROR, "block", 8, 16),
            "21:25: " + getCheckMessage(MSG_CHILD_ERROR, "case", 24, 12),
            "22:9: " + getCheckMessage(MSG_CHILD_ERROR, "block", 8, 16),
            "27:9: " + getCheckMessage(MSG_CHILD_ERROR, "block", 8, 20),
            "29:1: " + getCheckMessage(MSG_CHILD_ERROR, "block", 0, 16),
            "30:1: " + getCheckMessage(MSG_ERROR, "yield", 0, 16),
            "34:5: " + getCheckMessage(MSG_CHILD_ERROR, "block", 4, 20),
            "44:1: " + getCheckMessage(MSG_CHILD_ERROR, "block", 0, 16),
            "46:21: " + getCheckMessage(MSG_CHILD_ERROR, "case", 20, 12),
            "47:1: " + getCheckMessage(MSG_CHILD_ERROR, "block", 0, 16),
            "51:9: " + getCheckMessage(MSG_CHILD_ERROR, "block", 8, 20),
            "56:33: " + getCheckMessage(MSG_CHILD_ERROR, "block", 32, 20),
        };

        verifyWarns(checkConfig,
                getPath("InputIndentationCheckSwitchExpression.java"),
                expected);
    }

    @Test
    public void testIndentationYieldStatement() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = {
            "23:13: " + getCheckMessage(MSG_ERROR, "yield", 12, 16),
            "28:9: " + getCheckMessage(MSG_CHILD_ERROR, "yield", 8, 16),
            "40:5: " + getCheckMessage(MSG_ERROR, "yield", 4, 16),
            "41:9: " + getCheckMessage(MSG_CHILD_ERROR, "yield", 8, 16),
            "71:1: " + getCheckMessage(MSG_ERROR, "yield", 0, 16),
            "74:37: " + getCheckMessage(MSG_ERROR, "yield", 36, 16),
        };

        verifyWarns(checkConfig,
            getPath("InputIndentationYieldStatement.java"),
            expected);
    }

    @Test
    public void testIndentationSwitchExpressionCorrect() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig,
            getPath("InputIndentationCheckSwitchExpressionCorrect.java"),
            expected);
    }

    @Test
    public void testIndentationSwitchExpressionDeclaration() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("lineWrappingIndentation", "8");
        final String[] expected = {
            "33:17: " + getCheckMessage(MSG_CHILD_ERROR, "case", 16, 12),
            "34:17: " + getCheckMessage(MSG_CHILD_ERROR, "case", 16, 12),
            "41:17: " + getCheckMessage(MSG_CHILD_ERROR, "case", 16, 12),
            "42:17: " + getCheckMessage(MSG_CHILD_ERROR, "case", 16, 12),
            "49:9: " + getCheckMessage(MSG_CHILD_ERROR, "case", 8, 12),
            "50:9: " + getCheckMessage(MSG_CHILD_ERROR, "case", 8, 12),
            "57:9: " + getCheckMessage(MSG_CHILD_ERROR, "case", 8, 12),
            "58:9: " + getCheckMessage(MSG_CHILD_ERROR, "case", 8, 12),
        };
        verifyWarns(checkConfig,
            getPath("InputIndentationCheckSwitchExpressionDeclaration.java"),
            expected);
    }

    @Test
    public void testIndentationSwitchExpressionDeclarationLeftCurlyNewLine() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = {
            "34:5: " + getCheckMessage(MSG_ERROR, "switch lcurly", 4, 8),
            "42:5: " + getCheckMessage(MSG_ERROR, "switch lcurly", 4, 8),
            "50:13: " + getCheckMessage(MSG_ERROR, "switch lcurly", 12, 8),
            "58:13: " + getCheckMessage(MSG_ERROR, "switch lcurly", 12, 8),
        };
        verifyWarns(checkConfig,
            getPath(
                    "InputIndentationCheckSwitchExpressionDeclarationLCurlyNewLine.java"),
            expected);
    }

    @Test
    public void testIndentationRecords() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("throwsIndent", "4");
        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("forceStrictCondition", "false");

        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWarns(checkConfig,
            getPath("InputIndentationRecords.java"),
            expected);
    }

    @Test
    public void testIndentationRecordsAndCompactCtors() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = {
            "13:1: " + getCheckMessage(MSG_ERROR, "(", 0, 8),
            "25:1: " + getCheckMessage(MSG_ERROR, "String", 0, 12),
            "38:1: " + getCheckMessage(MSG_CHILD_ERROR, "compact ctor def", 0, 12),
            "48:8: " + getCheckMessage(MSG_ERROR, "record def ident", 0, 4),
            "53:1: " + getCheckMessage(MSG_ERROR, "compact ctor def rcurly", 0, 8),
            "61:1: " + getCheckMessage(MSG_ERROR, "ctor def rcurly", 0, 8),
        };

        verifyWarns(checkConfig,
            getPath("InputIndentationRecordsAndCompactCtors.java"),
            expected);
    }

    @Test
    public void testIndentationSwitchExpressionNewLine() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = {
            "30:13: " + getCheckMessage(MSG_ERROR, "lambda", 12, 16),
            "32:13: " + getCheckMessage(MSG_ERROR, "lambda", 12, 16),
        };

        verifyWarns(checkConfig,
            getPath("InputIndentationCheckSwitchExpressionNewLine.java"),
            expected);
    }

    @Test
    public void testIndentationMethodParenthesisOnNewLine() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = {
            "13:9: " + getCheckMessage(MSG_ERROR, "method def rparen", 8, 4),
            "18:9: " + getCheckMessage(MSG_ERROR, "method def rparen", 8, 4),
        };

        verifyWarns(checkConfig,
                getPath("InputIndentationCheckMethodParenOnNewLine.java"),
                expected);
    }

    @Test
    public void testIndentationMethodParenthesisOnNewLine1() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        final String[] expected = {
            "11:10: " + getCheckMessage(MSG_ERROR, "2", 9, 12),
            "17:8: " + getCheckMessage(MSG_ERROR, "int", 7, 8),
            "18:9: " + getCheckMessage(MSG_ERROR, "method def rparen", 8, 4),
        };

        verifyWarns(checkConfig,
                getPath("InputIndentationCheckMethodParenOnNewLine1.java"),
                expected);
    }

    @Test
    public void testIndentationLineWrappedRecordDeclaration() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("throwsIndent", "4");
        checkConfig.addProperty("arrayInitIndent", "4");
        checkConfig.addProperty("lineWrappingIndentation", "4");

        final String[] expected = {
            "33:1: " + getCheckMessage(MSG_ERROR, ")", 0, 4),
            "55:11: " + getCheckMessage(MSG_ERROR, "interface def ident", 0, 4),
            "56:1: " + getCheckMessage(MSG_ERROR, "method def modifier", 0, 8),
            "57:1: " + getCheckMessage(MSG_ERROR, "void", 0, 4),
            "58:1: " + getCheckMessage(MSG_ERROR, "method", 0, 4),
            "59:1: " + getCheckMessage(MSG_ERROR, "throws", 0, 4),
            "60:1: " + getCheckMessage(MSG_ERROR, "IOException", 0, 4),
            "61:1: " + getCheckMessage(MSG_ERROR, "method def rcurly", 0, 8),
            "62:1: " + getCheckMessage(MSG_ERROR, "interface def rcurly", 0, 4),
            "75:8: " + getCheckMessage(MSG_ERROR, "record def ident", 0, 4),
            "76:1: " + getCheckMessage(MSG_ERROR, "record def rparen", 0, 4),
            "77:1: " + getCheckMessage(MSG_ERROR, "implements", 0, 4),
            "78:1: " + getCheckMessage(MSG_ERROR, "SimpleInterface2", 0, 4),
            "79:8: " + getCheckMessage(MSG_ERROR, "record def ident", 0, 8),
            "80:1: " + getCheckMessage(MSG_ERROR, "(", 0, 4),
            "81:1: " + getCheckMessage(MSG_ERROR, "record def rparen", 0, 8),
            "82:1: " + getCheckMessage(MSG_ERROR, "record def lcurly", 0, 8),
            "83:1: " + getCheckMessage(MSG_ERROR, "record def rcurly", 0, 8),
            "84:1: " + getCheckMessage(MSG_ERROR, "record def rcurly", 0, 4),
        };

        verifyWarns(checkConfig,
            getPath("InputIndentationLineWrappedRecordDeclaration.java"),
            expected);
    }

    @Test
    public void testIndentationAnnotationFieldDefinition() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("throwsIndent", "8");
        checkConfig.addProperty("forceStrictCondition", "true");

        final String[] expected = {
            "17:5: " + getCheckMessage(MSG_ERROR, "annotation field def modifier", 4, 8),
            "18:13: " + getCheckMessage(MSG_ERROR, "annotation field def modifier", 12, 8),
            "25:5: " + getCheckMessage(MSG_ERROR, "member def type", 4, 8),
            "26:5: " + getCheckMessage(MSG_ERROR, "member def type", 4, 8),
        };

        verifyWarns(checkConfig, getPath("InputIndentationAnnotationFieldDefinition.java"),
                expected);
    }

    @Test
    public void testIndentationLongConcatenatedString() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");

        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWarns(checkConfig, getPath("InputIndentationLongConcatenatedString.java"),
                expected);
    }

    @Test
    public void testIndentationLongConcatenatedString1() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");

        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWarns(checkConfig, getPath("InputIndentationLongConcatenatedString1.java"),
                expected);
    }

    @Test
    public void testIndentationLongConcatenatedString2() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");

        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWarns(checkConfig, getPath("InputIndentationLongConcatenatedString2.java"),
                expected);
    }

    @Test
    public void testIndentationLongConcatenatedString3() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");

        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWarns(checkConfig, getPath("InputIndentationLongConcatenatedString3.java"),
                expected);
    }

    @Test
    public void testIndentationLongConcatenatedString4() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");

        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWarns(checkConfig, getPath("InputIndentationLongConcatenatedString4.java"),
                expected);
    }

    @Test
    public void testIndentationLongConcatenatedString5() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");

        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWarns(checkConfig, getPath("InputIndentationLongConcatenatedString5.java"),
                expected);
    }

    @Test
    public void testIndentationLongConcatenatedString6() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");

        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWarns(checkConfig, getPath("InputIndentationLongConcatenatedString6.java"),
                expected);
    }

    @Test
    public void testIndentationLongConcatenatedString7() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");

        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWarns(checkConfig, getPath("InputIndentationLongConcatenatedString7.java"),
                expected);
    }

    @Test
    public void testIndentationLongConcatenatedString8() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");

        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWarns(checkConfig, getPath("InputIndentationLongConcatenatedString8.java"),
                expected);
    }

    @Test
    public void testIndentationLongConcatenatedString9() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");

        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWarns(checkConfig, getPath("InputIndentationLongConcatenatedString9.java"),
                expected);
    }

    @Test
    public void testIndentationLongConcatenatedString10() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");

        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWarns(checkConfig, getPath("InputIndentationLongConcatenatedString10.java"),
                expected);
    }

    @Test
    public void testIndentationLongConcatenatedString11() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");

        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verifyWarns(checkConfig, getPath("InputIndentationLongConcatenatedString11.java"),
                expected);
    }

    @Test
    public void testIndentationLineBreakVariableDeclaration()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");

        final String fileName = getPath("InputIndentationLineBreakVariableDeclaration.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testIndentationSwitchExpressionOnStartOfTheLine() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "2");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("throwsIndent", "4");
        checkConfig.addProperty("lineWrappingIndentation", "4");

        final String[] expected = {
            "40:7: " + getCheckMessage(MSG_ERROR, "switch", 6, 8),
            "41:9: " + getCheckMessage(MSG_CHILD_ERROR, "case", 8, 10),
            "42:9: " + getCheckMessage(MSG_CHILD_ERROR, "case", 8, 10),
            "43:9: " + getCheckMessage(MSG_CHILD_ERROR, "case", 8, 10),
            "44:7: " + getCheckMessage(MSG_ERROR, "switch rcurly", 6, 8),
            "49:11: " + getCheckMessage(MSG_ERROR, "switch", 10, 8),
            "50:13: " + getCheckMessage(MSG_CHILD_ERROR, "case", 12, 10),
            "51:13: " + getCheckMessage(MSG_CHILD_ERROR, "case", 12, 10),
            "52:13: " + getCheckMessage(MSG_CHILD_ERROR, "case", 12, 10),
        };

        verifyWarns(checkConfig,
                getPath("InputIndentationSwitchOnStartOfLine.java"), expected);
    }

    @Test
    public void testIndentationSwitchExpressionWrappingIndentation() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "2");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("lineWrappingIndentation", "4");

        final String fileName = getPath(
                "InputIndentationSwitchExpressionWrappingIndentation.java");
        final String[] expected = {
            "41:7: " + getCheckMessage(MSG_ERROR, "switch", 6, 8),
            "42:9: " + getCheckMessage(MSG_CHILD_ERROR, "case", 8, 10),
            "43:9: " + getCheckMessage(MSG_CHILD_ERROR, "case", 8, 10),
            "44:9: " + getCheckMessage(MSG_CHILD_ERROR, "case", 8, 10),
            "45:7: " + getCheckMessage(MSG_ERROR, "switch rcurly", 6, 8),
            "51:9: " + getCheckMessage(MSG_CHILD_ERROR, "lambda", 8, 10),
            "52:11: " + getCheckMessage(MSG_CHILD_ERROR, "case", 10, 12),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInputIndentationLambdaChild() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "2");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("lineWrappingIndentation", "4");

        final String fileName = getPath(
                "InputIndentationLambdaChild.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testInputLambdaAndChildOnTheSameLine() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "2");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("lineWrappingIndentation", "4");

        final String fileName = getNonCompilablePath(
                "InputIndentationLambdaAndChildOnTheSameLine.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testIndentationPatternMatchingForSwitch()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("throwsIndent", "8");

        final String fileName = getNonCompilablePath(
                "InputIndentationPatternMatchingForSwitch.java");
        final String[] expected = {
            "21:13: " + getCheckMessage(MSG_CHILD_ERROR, "case", 12, 16),
            "54:13: " + getCheckMessage(MSG_CHILD_ERROR, "case", 12, 16),
            "69:13: " + getCheckMessage(MSG_CHILD_ERROR, "case", 12, 16),
            "70:13: " + getCheckMessage(MSG_CHILD_ERROR, "case", 12, 16),
            "75:5: " + getCheckMessage(MSG_CHILD_ERROR, "case", 4, 16),
            "76:5: " + getCheckMessage(MSG_CHILD_ERROR, "case", 4, 16),
            "87:1: " + getCheckMessage(MSG_CHILD_ERROR, "case", 0, 16),
            "88:1: " + getCheckMessage(MSG_CHILD_ERROR, "case", 0, 16),
            "89:1: " + getCheckMessage(MSG_CHILD_ERROR, "case", 0, 16),
            "90:1: " + getCheckMessage(MSG_ERROR, "lambda", 0, 16),
            "91:1: " + getCheckMessage(MSG_CHILD_ERROR, "lambda", 0, 20),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testIndentationSingleSwitchStatementsWithoutCurly()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("throwsIndent", "4");

        final String fileName = getPath(
                "InputIndentationCheckSingleSwitchStatementsWithoutCurly.java");
        final String[] expected = {
            "31:13: " + getCheckMessage(MSG_ERROR, "lambda", 12, 16),
            "32:13: " + getCheckMessage(MSG_CHILD_ERROR, "lambda", 12, 20),
            "33:9: " + getCheckMessage(MSG_CHILD_ERROR, "case", 8, 12),
            "34:1: " + getCheckMessage(MSG_CHILD_ERROR, "lambda", 0, 16),
            "44:25: " + getCheckMessage(MSG_CHILD_ERROR, "lambda", 24, 16),
            "47:13: " + getCheckMessage(MSG_CHILD_ERROR, "block", 12, 16),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testIndentationRecordPattern()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("throwsIndent", "8");

        final String fileName = getNonCompilablePath(
                "InputIndentationRecordPattern.java");
        final String[] expected = {
            "19:17: " + getCheckMessage(MSG_ERROR, "ColoredPoint", 16, 12),
            "24:9: " + getCheckMessage(MSG_ERROR, "ColoredPoint", 8, 12),
            "29:17: " + getCheckMessage(MSG_ERROR, "ColoredPoint", 16, 12),
            "34:9: " + getCheckMessage(MSG_CHILD_ERROR, "if", 8, 12),
            "37:1: " + getCheckMessage(MSG_CHILD_ERROR, "if", 0, 12),
            "39:1: " + getCheckMessage(MSG_CHILD_ERROR, "if", 0, 12),
            "40:1: " + getCheckMessage(MSG_CHILD_ERROR, "if", 0, 12),
            "41:1: " + getCheckMessage(MSG_CHILD_ERROR, "if", 0, 12),
            "42:1: " + getCheckMessage(MSG_CHILD_ERROR, "if", 0, 12),
            "56:17: " + getCheckMessage(MSG_ERROR, "Rectangle", 16, 12),
            "57:17: " + getCheckMessage(MSG_ERROR, "ColoredPoint", 16, 12),
            "58:25: " + getCheckMessage(MSG_ERROR, "boolean", 24, 12),
            "59:17: " + getCheckMessage(MSG_ERROR, "int", 16, 12),
            "60:25: " + getCheckMessage(MSG_ERROR, "_", 24, 12),
            "61:17: " + getCheckMessage(MSG_ERROR, "ColoredPoint", 16, 12),
            "62:17: " + getCheckMessage(MSG_ERROR, ")", 16, 8),
            "67:1: " + getCheckMessage(MSG_CHILD_ERROR, "if", 0, 12),
            "66:1: " + getCheckMessage(MSG_CHILD_ERROR, "if", 0, 12),
            "68:9: " + getCheckMessage(MSG_CHILD_ERROR, "if", 8, 12),
            "69:9: " + getCheckMessage(MSG_CHILD_ERROR, "if", 8, 12),
            "70:9: " + getCheckMessage(MSG_CHILD_ERROR, "if", 8, 12),
            "71:1: " + getCheckMessage(MSG_CHILD_ERROR, "if", 0, 12),
            "72:9: " + getCheckMessage(MSG_CHILD_ERROR, "if", 8, 12),
            "81:13: " + getCheckMessage(MSG_ERROR, ")", 12, 8),
            "89:9: " + getCheckMessage(MSG_CHILD_ERROR, "case", 8, 16),
            "90:9: " + getCheckMessage(MSG_CHILD_ERROR, "case", 8, 16),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testIndentationCodeBlocks1() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "2");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("throwsIndent", "4");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        final String[] expected = {
            "17:5: " + getCheckMessage(MSG_ERROR, "block lcurly", 4, 2),
            "18:7: " + getCheckMessage(MSG_CHILD_ERROR, "block", 6, 4),
            "19:5: " + getCheckMessage(MSG_ERROR, "block rcurly", 4, 2),
            "30:5: " + getCheckMessage(MSG_ERROR, "block lcurly", 4, 2),
            "31:7: " + getCheckMessage(MSG_CHILD_ERROR, "block", 6, 4),
            "32:5: " + getCheckMessage(MSG_ERROR, "block rcurly", 4, 2),
        };
        verifyWarns(checkConfig, getPath("InputIndentationCodeBlocks1.java"), expected);
    }

    @Test
    public void testIndentationCodeBlocks2() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "2");
        checkConfig.addProperty("throwsIndent", "4");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        final String[] expected = {
            "45:13: " + getCheckMessage(MSG_ERROR, "for lcurly", 12, 14),
            "47:13: " + getCheckMessage(MSG_ERROR, "for rcurly", 12, 14),
        };
        verifyWarns(checkConfig,
                getPath("InputIndentationCodeBlocks2.java"), expected);
    }

    @Test
    public void testIndentationAnnotationArray() throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "2");
        checkConfig.addProperty("braceAdjustment", "2");
        checkConfig.addProperty("caseIndent", "2");
        checkConfig.addProperty("throwsIndent", "4");
        checkConfig.addProperty("lineWrappingIndentation", "4");
        checkConfig.addProperty("arrayInitIndent", "2");

        final String fileName = getPath(
                "InputIndentationAnnotationArray.java");
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWarns(checkConfig, fileName, expected);
    }

    @Test
    public void testIndentationSealedClasses()
            throws Exception {
        final DefaultConfiguration checkConfig = createModuleConfig(IndentationCheck.class);
        checkConfig.addProperty("forceStrictCondition", "true");
        checkConfig.addProperty("tabWidth", "4");
        checkConfig.addProperty("basicOffset", "4");
        checkConfig.addProperty("braceAdjustment", "0");
        checkConfig.addProperty("caseIndent", "4");
        checkConfig.addProperty("throwsIndent", "8");

        final String fileName = getNonCompilablePath(
                "InputIndentationSealedClasses.java");
        final String[] expected = {
            "14:1: " + getCheckMessage(MSG_ERROR, "class def modifier", 0, 4),
            "15:2: " + getCheckMessage(MSG_ERROR, "class", 1, 4),
            "16:6: " + getCheckMessage(MSG_ERROR, "permits", 5, 4),
            "19:5: " + getCheckMessage(MSG_ERROR, "class", 4, 8),
            "20:5: " + getCheckMessage(MSG_ERROR, "permits", 4, 8),
            "28:1: " + getCheckMessage(MSG_ERROR, "class def modifier", 0, 4),
            "29:9: " + getCheckMessage(MSG_ERROR, "extends", 8, 4),
            "32:5: " + getCheckMessage(MSG_ERROR, "extends", 4, 8),
            "38:5: " + getCheckMessage(MSG_ERROR, "class", 4, 8),
            "39:1: " + getCheckMessage(MSG_ERROR, "permits", 0, 8),
            "40:13: " + getCheckMessage(MSG_ERROR, "C", 12, 8),
            "48:5: " + getCheckMessage(MSG_ERROR, "class", 4, 8),
            "49:5: " + getCheckMessage(MSG_ERROR, "C", 4, 8),
            "55:1: " + getCheckMessage(MSG_ERROR, "class def modifier", 0, 4),
            "56:9: " + getCheckMessage(MSG_ERROR, "class", 8, 4),
        };
        verifyWarns(checkConfig, fileName, expected);
    }

    private static final class IndentAudit implements AuditListener {

        private final IndentComment[] comments;
        private int position;

        private IndentAudit(IndentComment... comments) {
            this.comments = Arrays.copyOf(comments, comments.length);
        }

        @Override
        public void auditStarted(AuditEvent event) {
            // No code needed
        }

        @Override
        public void auditFinished(AuditEvent event) {
            // No code needed
        }

        @Override
        public void fileStarted(AuditEvent event) {
            // No code needed
        }

        @Override
        public void fileFinished(AuditEvent event) {
            // No code needed
        }

        @Override
        public void addError(AuditEvent event) {
            final int line = event.getLine();
            final String message = event.getMessage();

            assertWithMessage(
                    "found a warning when none was expected for #%s at line %s with message %s",
                    position, line, message)
                .that(position)
                .isLessThan(comments.length);

            final IndentComment comment = comments[position];
            position++;

            final String possibleExceptedMessages = Arrays.stream(comment.getExpectedMessages())
                    .reduce("", (cur, next) -> cur + "\"" + next + "\", ");
            final String assertMessage = String.format(
                    Locale.ROOT,
                    "input expected warning #%d at line %d to report one of the following: %s"
                            + "but got instead: %d: %s",
                    position, comment.getLineNumber(), possibleExceptedMessages, line, message);
            assertWithMessage(assertMessage)
                    .that(line == comment.getLineNumber() && Arrays
                            .stream(comment.getExpectedMessages()).anyMatch(message::endsWith))
                    .isTrue();
        }

        @Override
        public void addException(AuditEvent event, Throwable throwable) {
            // No code needed
        }

    }

    private static final class IndentComment {

        /** Used to locate the index of argument zero of error messages. */
        private static final String FAKE_ARGUMENT_ZERO = "##0##";
        private final int lineNumber;
        private final int indent;
        /** Used for when violations report nodes not first on the line. */
        private final int indentOffset;
        private final boolean expectedNonStrict;
        private final String expectedWarning;
        private final boolean warning;

        private IndentComment(IndentComment original, int newLineNumber) {
            lineNumber = newLineNumber;
            indent = original.indent;
            indentOffset = original.indentOffset;
            expectedNonStrict = original.expectedNonStrict;
            expectedWarning = original.expectedWarning;
            warning = original.warning;
        }

        private IndentComment(Matcher match, int lineNumber) {
            this.lineNumber = lineNumber;
            indent = Integer.parseInt(match.group(1));
            if (match.group(2) == null) {
                indentOffset = 0;
            }
            else {
                indentOffset = Integer.parseInt(match.group(2));
            }
            expectedNonStrict = match.group(3) != null;
            expectedWarning = match.group(4).replace(",", ", ");
            warning = match.group(5) != null;
        }

        public String[] getExpectedMessages() {
            final String[] expectedMessages;
            if (expectedWarning.contains(",")) {
                expectedMessages = new String[] {
                    getExpectedMessagePostfix(MSG_ERROR_MULTI),
                    getExpectedMessagePostfix(MSG_CHILD_ERROR_MULTI),
                };
            }
            else {
                expectedMessages = new String[] {
                    getExpectedMessagePostfix(MSG_ERROR),
                    getExpectedMessagePostfix(MSG_CHILD_ERROR),
                };
            }
            return expectedMessages;
        }

        private String getExpectedMessagePostfix(final String messageKey) {
            final String msg = getCheckMessage(IndentationCheck.class, messageKey,
                    FAKE_ARGUMENT_ZERO, indent + indentOffset, expectedWarning);
            final int indexOfMsgPostfix = msg.indexOf(FAKE_ARGUMENT_ZERO)
                    + FAKE_ARGUMENT_ZERO.length();
            return msg.substring(indexOfMsgPostfix);
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public int getIndent() {
            return indent;
        }

        public int getIndentOffset() {
            return indentOffset;
        }

        public boolean isExpectedNonStrict() {
            return expectedNonStrict;
        }

        public String getExpectedWarning() {
            return expectedWarning;
        }

        public boolean isWarning() {
            return warning;
        }

    }

}

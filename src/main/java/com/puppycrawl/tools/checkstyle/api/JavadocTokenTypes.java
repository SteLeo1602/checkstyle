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

package com.puppycrawl.tools.checkstyle.api;

import com.puppycrawl.tools.checkstyle.grammar.javadoc.JavadocParser;

/**
 * Contains the constants for all the tokens contained in the Abstract
 * Syntax Tree for the javadoc grammar.
 *
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html">
 *     javadoc - The Java API Documentation Generator</a>
 */
public final class JavadocTokenTypes {

    // ------------------------------------------------------------------------------------------ //
    // -----------------        JAVADOC TAGS          ------------------------------------------- //
    // ------------------------------------------------------------------------------------------ //

    /**
     * '@return' literal in {@code @return} Javadoc tag.
     *
     * <p>Such Javadoc tag can have one argument - {@link #DESCRIPTION}</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @return true if file exists}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     * JAVADOC_TAG -> JAVADOC_TAG
     *  |--RETURN_LITERAL -> @return
     *  |--WS ->
     *  `--DESCRIPTION -> DESCRIPTION
     *      |--TEXT -> true if file exists
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDCDBGG">
     *     Oracle Docs</a>
     * @see #JAVADOC_TAG
     */
    public static final int RETURN_LITERAL = JavadocParser.RETURN_LITERAL;

    /**
     * '{@literal @}deprecated' literal in {@literal @}deprecated Javadoc tag.
     *
     * <p>Such Javadoc tag can have one argument - {@link #DESCRIPTION}</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @deprecated It is deprecated method}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     * JAVADOC_TAG -> JAVADOC_TAG
     *  |--DEPRECATED_LITERAL -> @deprecated
     *  |--WS ->
     *  `--DESCRIPTION -> DESCRIPTION
     *      |--TEXT -> It is deprecated method
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#deprecated">
     *     Oracle Docs</a>
     * @see #JAVADOC_TAG
     */
    public static final int DEPRECATED_LITERAL = JavadocParser.DEPRECATED_LITERAL;

    /**
     * '@since' literal in {@code @since} Javadoc tag.
     *
     * <p>Such Javadoc tag can have one argument - {@link #DESCRIPTION}</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @since 3.4 RELEASE}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     * JAVADOC_TAG -> JAVADOC_TAG
     *  |--SINCE_LITERAL -> @since
     *  |--WS ->
     *  `--DESCRIPTION -> DESCRIPTION
     *      |--TEXT -> 3.4 RELEASE
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDHGJGD">
     *     Oracle Docs</a>
     * @see #JAVADOC_TAG
     */
    public static final int SINCE_LITERAL = JavadocParser.SINCE_LITERAL;

    /**
     * '@serialData' literal in {@code @serialData} Javadoc tag.
     *
     * <p>Such Javadoc tag can have one argument - {@link #DESCRIPTION}</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @serialData Two values of Integer type}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     * JAVADOC_TAG -> JAVADOC_TAG
     *  |--SERIAL_DATA_LITERAL -> @serialData
     *  |--WS ->
     *  `--DESCRIPTION -> DESCRIPTION
     *      |--TEXT -> Two values of Integer type
     * }
     * </pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDJBFDB">
     *     Oracle Docs</a>
     * @see #JAVADOC_TAG
     */
    public static final int SERIAL_DATA_LITERAL = JavadocParser.SERIAL_DATA_LITERAL;

    /**
     * '@serialField' literal in {@code @serialField} Javadoc tag.
     *
     * <p>Such Javadoc tag can have three arguments:</p>
     * <ol>
     * <li>{@link #FIELD_NAME}</li>
     * <li>{@link #FIELD_TYPE}</li>
     * <li>{@link #DESCRIPTION}</li>
     * </ol>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @serialField counter Integer objects counter}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     * JAVADOC_TAG -> JAVADOC_TAG
     *  |--SERIAL_FIELD_LITERAL -> @serialField
     *  |--WS ->
     *  |--FIELD_NAME -> counter
     *  |--WS ->
     *  |--FIELD_TYPE -> Integer
     *  |--WS ->
     *  `--DESCRIPTION -> DESCRIPTION
     *      |--TEXT -> objects counter
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDGHIDG">
     *     Oracle Docs</a>
     * @see #JAVADOC_TAG
     */
    public static final int SERIAL_FIELD_LITERAL = JavadocParser.SERIAL_FIELD_LITERAL;

    /**
     * '@param' literal in {@code @param} Javadoc tag.
     *
     * <p>Such Javadoc tag can have two arguments:</p>
     * <ol>
     * <li>{@link #PARAMETER_NAME}</li>
     * <li>{@link #DESCRIPTION}</li>
     * </ol>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @param value The parameter of method.}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     * JAVADOC_TAG -> JAVADOC_TAG
     *  |--PARAM_LITERAL -> @param
     *  |--WS ->
     *  |--PARAMETER_NAME -> value
     *  |--WS ->
     *  `--DESCRIPTION -> DESCRIPTION
     *      |--TEXT -> The parameter of method.
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDHJECF">
     *     Oracle Docs</a>
     * @see #JAVADOC_TAG
     */
    public static final int PARAM_LITERAL = JavadocParser.PARAM_LITERAL;

    /**
     * '@see' literal in {@code @see} Javadoc tag.
     *
     * <p>Such Javadoc tag can have one argument - {@link #REFERENCE}</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @see org.apache.utils.Lists.Comparator#compare(Object)}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     *   JAVADOC_TAG -> JAVADOC_TAG
     *    |--SEE_LITERAL -> @see
     *    |--WS ->
     *    |--REFERENCE -> REFERENCE
     *        |--PACKAGE_CLASS -> org.apache.utils.Lists.Comparator
     *        |--HASH -> #
     *        |--MEMBER -> compare
     *        `--PARAMETERS -> PARAMETERS
     *            |--LEFT_BRACE -> (
     *            |--ARGUMENT -> Object
     *            `--RIGHT_BRACE -> )
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDDIEDI">
     *     Oracle Docs</a>
     * @see #JAVADOC_TAG
     */
    public static final int SEE_LITERAL = JavadocParser.SEE_LITERAL;

    /**
     * '@serial' literal in {@code @serial} Javadoc tag.
     *
     * <p>Such Javadoc tag can have one argument - {@link #REFERENCE} or {@link #LITERAL_EXCLUDE}
     * or {@link #LITERAL_INCLUDE}</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @serial include}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     *   |--JAVADOC_TAG -> JAVADOC_TAG
     *       |--SERIAL_LITERAL -> @serial
     *       |--WS
     *       |--LITERAL_INCLUDE -> include
     * }</pre>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @serial serialized company name}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     *   |--JAVADOC_TAG-> JAVADOC_TAG
     *       |--SERIAL_LITERAL -> @serial
     *       |--WS
     *       |--DESCRIPTION -> DESCRIPTION
     *           |--TEXT -> serialized company name
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDHDECF">
     *     Oracle Docs</a>
     * @see #JAVADOC_TAG
     */
    public static final int SERIAL_LITERAL = JavadocParser.SERIAL_LITERAL;

    /**
     * '@version' literal in {@code @version} Javadoc tag.
     *
     * <p>Such Javadoc tag can have one argument - {@link #DESCRIPTION}</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @version 1.3}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     *   JAVADOC_TAG -> JAVADOC_TAG
     *    |--VERSION_LITERAL -> @version
     *    |--WS ->
     *    `--DESCRIPTION -> DESCRIPTION
     *        |--TEXT -> 1.3
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDCHBAE">
     *     Oracle Docs</a>
     * @see #JAVADOC_TAG
     */
    public static final int VERSION_LITERAL = JavadocParser.VERSION_LITERAL;

    /**
     * '@exception' literal in {@code @exception} Javadoc tag.
     *
     * <p>Such Javadoc tag can have two argument - {@link #CLASS_NAME} and {@link #DESCRIPTION}</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @exception SQLException if query is not correct}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     *   JAVADOC_TAG -> JAVADOC_TAG
     *    |--EXCEPTION_LITERAL -> @exception
     *    |--WS ->
     *    |--CLASS_NAME -> SQLException
     *    |--WS ->
     *    `--DESCRIPTION -> DESCRIPTION
     *        `--TEXT -> if query is not correct
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDCEAHH">
     *     Oracle Docs</a>
     * @see #JAVADOC_TAG
     */
    public static final int EXCEPTION_LITERAL = JavadocParser.EXCEPTION_LITERAL;

    /**
     * '@throws' literal in {@code @throws} Javadoc tag.
     *
     * <p>Such Javadoc tag can have two argument - {@link #CLASS_NAME} and {@link #DESCRIPTION}</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @throws SQLException if query is not correct}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     *   JAVADOC_TAG -> JAVADOC_TAG
     *      |--THROWS_LITERAL -> @throws
     *      |--WS ->
     *      |--CLASS_NAME -> SQLException
     *      |--WS ->
     *      `--DESCRIPTION -> DESCRIPTION
     *          |--TEXT -> if query is not correct
     *          |--NEWLINE -> \r\n
     *          `--TEXT ->
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDCHAHD">
     *     Oracle Docs</a>
     * @see #JAVADOC_TAG
     */
    public static final int THROWS_LITERAL = JavadocParser.THROWS_LITERAL;

    /**
     * '@author' literal in {@code @author} Javadoc tag.
     *
     * <p>Such Javadoc tag can have one argument - {@link #DESCRIPTION}</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @author Baratali Izmailov}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     *   JAVADOC_TAG -> JAVADOC_TAG
     *      |--AUTHOR_LITERAL -> @author
     *      |--WS ->
     *      `--DESCRIPTION -> DESCRIPTION
     *          |--TEXT -> Baratali Izmailov
     *          |--NEWLINE -> \r\n
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDCBAHA">
     *     Oracle Docs</a>
     * @see #JAVADOC_TAG
     */
    public static final int AUTHOR_LITERAL = JavadocParser.AUTHOR_LITERAL;

    /**
     * Name of custom Javadoc tag (or Javadoc inline tag).
     *
     * <p>Such Javadoc tag can have one argument - {@link #DESCRIPTION}</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @myJavadocTag some magic}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     *   JAVADOC_TAG --> JAVADOC_TAG
     *       |--CUSTOM_NAME --> @myJavadocTag
     *       |--WS -->
     *       `--DESCRIPTION --> DESCRIPTION
     *           |--TEXT --> some magic
     * }</pre>
     */
    public static final int CUSTOM_NAME = JavadocParser.CUSTOM_NAME;

    /**
     * First child of {@link #JAVADOC_INLINE_TAG} that represents left curly brace '{'.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code Comparable<E>}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     *    JAVADOC_INLINE_TAG --> JAVADOC_INLINE_TAG
     *     |--JAVADOC_INLINE_TAG_START --> {
     *     |--CODE_LITERAL --> @code
     *     |--WS -->
     *     |--TEXT --> Comparable<E>
     *     `--JAVADOC_INLINE_TAG_END --> }
     * }</pre>
     */
    public static final int JAVADOC_INLINE_TAG_START = JavadocParser.JAVADOC_INLINE_TAG_START;

    /**
     * Last child of {@link #JAVADOC_INLINE_TAG} that represents right curly brace '}'.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code Comparable<E>}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     *   JAVADOC_INLINE_TAG --> JAVADOC_INLINE_TAG
     *    |--JAVADOC_INLINE_TAG_START --> {
     *    |--CODE_LITERAL --> @code
     *    |--WS -->
     *    |--TEXT --> Comparable<E>
     *    `--JAVADOC_INLINE_TAG_END --> }
     * }</pre>
     */
    public static final int JAVADOC_INLINE_TAG_END = JavadocParser.JAVADOC_INLINE_TAG_END;

    /**
     * '@code' literal in {&#64;code} Javadoc inline tag.
     *
     * <p>Such Javadoc inline tag can have such child nodes:</p>
     * <ul>
     * <li>{@link #NEWLINE}</li>
     * <li>{@link #WS}</li>
     * <li>{@link #TEXT}</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code Comparable<E>}</pre>
     * <b>Tree:</b>
     * <pre>{@code
     *   JAVADOC_TAG -> JAVADOC_TAG
     *    |--CUSTOM_NAME -> @code
     *    |--WS ->
     *    `--DESCRIPTION -> DESCRIPTION
     *        |--TEXT -> Comparable<E>
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDFHHBB">
     *     Oracle Docs</a>
     * @see #JAVADOC_INLINE_TAG
     */
    public static final int CODE_LITERAL = JavadocParser.CODE_LITERAL;

    /**
     * '@docRoot' literal in {&#64;docRoot} Javadoc inline tag.
     *
     * <p>Such Javadoc inline tag does not have any arguments and can have such child nodes:</p>
     * <ul>
     * <li>{@link #NEWLINE}</li>
     * <li>{@link #WS}</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @docRoot}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     * |--JAVADOC_INLINE_TAG -> JAVADOC_INLINE_TAG
     *      |--JAVADOC_INLINE_TAG_START -> {
     *      |--DOC_ROOT_LITERAL -> @docRoot
     *      `--JAVADOC_INLINE_TAG_END -> }
     * }</pre>
     *
     * <pre>{@code Example :{@docRoot
     * } in a Javadoc comment.
     * }</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   |--JAVADOC_INLINE_TAG -> JAVADOC_INLINE_TAG
     *     |--JAVADOC_INLINE_TAG_START -> {
     *     |--DOC_ROOT_LITERAL -> @docRoot
     *     |--NEWLINE -> \r\n
     *     |--LEADING_ASTERISK ->       *
     *     |--WS ->
     *     `--JAVADOC_INLINE_TAG_END -> }
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDBACBF">
     *     Oracle Docs</a>
     * @see #JAVADOC_INLINE_TAG
     */
    public static final int DOC_ROOT_LITERAL = JavadocParser.DOC_ROOT_LITERAL;

    /**
     * '@link' literal in {&#64;link} Javadoc inline tag.
     *
     * <p>Such Javadoc inline tag can have one argument - {@link #REFERENCE}</p>
     *
     * <p><b>Example:</b></p>
     *
     * <pre><code>{&#64;link org.apache.utils.Lists.Comparator#compare(Object)}</code></pre>
     *
     * <p><b>Tree:</b></p>
     *
     * <pre>
     * {@code
     *   JAVADOC_INLINE_TAG -> JAVADOC_INLINE_TAG
     *    |--JAVADOC_INLINE_TAG_START -> {
     *    |--LINK_LITERAL -> @link
     *    |--WS ->
     *    |--REFERENCE -> REFERENCE
     *    |   |--PACKAGE_CLASS -> org.apache.utils.Lists.Comparator
     *    |   |--HASH -> #
     *    |   |--MEMBER -> compare
     *    |   `--PARAMETERS -> PARAMETERS
     *    |       |--LEFT_BRACE -> (
     *    |       |--ARGUMENT -> Object
     *    |       `--RIGHT_BRACE -> )
     *    `--JAVADOC_INLINE_TAG_END -> }
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDDIECH">
     *     Oracle Docs</a>
     * @see #JAVADOC_INLINE_TAG
     * @noinspection HtmlTagCanBeJavadocTag
     * @noinspectionreason HtmlTagCanBeJavadocTag - encoded symbols were not decoded when
     *      replaced with Javadoc tag
     */
    public static final int LINK_LITERAL = JavadocParser.LINK_LITERAL;

    /**
     * '@inheritDoc' literal in {&#64;inheritDoc} Javadoc inline tag.
     *
     * <p>Such Javadoc inline tag does not have any arguments and can have such child nodes:</p>
     * <ul>
     * <li>{@link #NEWLINE}</li>
     * <li>{@link #WS}</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code {@inheritDoc} }</pre>
     * <b>Tree:</b>
     * <pre>{@code
     *   JAVADOC_INLINE_TAG -> JAVADOC_INLINE_TAG
     *    |--JAVADOC_INLINE_TAG_START -> {
     *    |--INHERIT_DOC_LITERAL -> @inheritDoc
     *    |--JAVADOC_INLINE_TAG_END -> }
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDGJCHC">
     *     Oracle Docs</a>
     * @see #JAVADOC_INLINE_TAG
     */
    public static final int INHERIT_DOC_LITERAL = JavadocParser.INHERIT_DOC_LITERAL;

    /**
     * '@linkplain' literal in {&#64;linkplain} Javadoc inline tag.
     *
     * <p>Such Javadoc inline tag can have one argument - {@link #REFERENCE}</p>
     *
     * <p><b>Example:</b></p>
     * <pre><code>{&#64;linkplain org.apache.utils.Lists.Comparator#compare(Object) compare}</code>
     * </pre>
     * <b>Tree:</b>
     * <pre>{@code
     *   JAVADOC_INLINE_TAG -> JAVADOC_INLINE_TAG
     *    |--JAVADOC_INLINE_TAG_START -> {
     *    |--LINKPLAIN_LITERAL -> @linkplain
     *    |--WS ->
     *    |--REFERENCE -> org.apache.utils.Lists.Comparator#compare(Object)
     *        |--PACKAGE_CLASS -> org.apache.utils
     *        |--DOT -> .
     *        |--CLASS -> Lists
     *        |--DOT -> .
     *        |--CLASS -> Comparator
     *        |--HASH -> #
     *        |--MEMBER -> compare
     *        |--PARAMETERS -> (Object)
     *            |--LEFT_BRACE -> (
     *            |--ARGUMENT -> Object
     *            |--RIGHT_BRACE -> )
     *     |--DESCRIPTION -> compare
     *         |--TEXT -> compare
     *     |--JAVADOC_INLINE_TAG_END -> }
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDGBICD">
     *     Oracle Docs</a>
     * @see #JAVADOC_INLINE_TAG
     * @noinspection HtmlTagCanBeJavadocTag
     * @noinspectionreason HtmlTagCanBeJavadocTag - encoded symbols were not decoded when
     *      replaced with Javadoc tag
     */
    public static final int LINKPLAIN_LITERAL = JavadocParser.LINKPLAIN_LITERAL;

    /**
     * '@literal' literal in {&#64;literal} Javadoc inline tag.
     *
     * <p>Such Javadoc inline tag can have such child nodes:</p>
     * <ul>
     * <li>{@link #NEWLINE}</li>
     * <li>{@link #WS}</li>
     * <li>{@link #TEXT}</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code {@literal #compare(Object)} }</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *     |--JAVADOC_INLINE_TAG -> JAVADOC_INLINE_TAG
     *        |--JAVADOC_INLINE_TAG_START -> {
     *        |--LITERAL_LITERAL -> @literal
     *        |--WS ->
     *        |--TEXT -> #compare(Object)
     *        `--JAVADOC_INLINE_TAG_END -> }
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDCFJDG">
     *     Oracle Docs</a>
     * @see #JAVADOC_INLINE_TAG
     */
    public static final int LITERAL_LITERAL = JavadocParser.LITERAL_LITERAL;

    /**
     * '@value' literal in {&#64;value} Javadoc inline tag.
     *
     * <p>Such Javadoc inline tag has one argument {@link #REFERENCE}
     * and can have such child nodes:</p>
     * <ul>
     * <li>{@link #NEWLINE}</li>
     * <li>{@link #WS}</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code {@value Integer#MAX_VALUE} }</pre>
     * <b>Tree:</b>
     * <pre>{@code
     *   JAVADOC_INLINE_TAG --> JAVADOC_INLINE_TAG
     *    |--JAVADOC_INLINE_TAG_START --> {
     *    |--VALUE_LITERAL --> @value
     *    |--WS -->
     *    |--REFERENCE --> REFERENCE
     *    |   |--PACKAGE_CLASS --> Integer
     *    |   |--HASH --> #
     *    |   `--MEMBER --> MAX_VALUE
     *    `--JAVADOC_INLINE_TAG_END --> }
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDDCDHH">
     *     Oracle Docs</a>
     * @see #JAVADOC_INLINE_TAG
     */
    public static final int VALUE_LITERAL = JavadocParser.VALUE_LITERAL;

    /**
     * PACKAGE_CLASS represents the package or class which has been referenced in the `@see`,
     * `@link`, `@linkplain` or `@value` javadoc tags. In the javadoc tree it shall be a child
     * of {@link #REFERENCE}.
     * <br>
     * <strong>IMPORTANT:</strong> Constructs like
     * {@code package.Class.NestedClassAtDepth1.NestedClassAtDepth2#member} are recognized by
     * the javadoc tool from oracle, and no assumptions like, package names wouldn't consist of
     * uppercase characters or class names begin with an uppercase character, are made.
     * Also, <i>the reference</i> in a javadoc tag can consist just a package name or a
     * simple class name or even a full class name. Thus, PACKAGE_CLASS can represent a
     * package name or a simple class name or a full class name i.e. checkstyle doesn't
     * resolve references at present.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @see org.apache.utils.Lists.Comparator#compare(Object)}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     * JAVADOC_TAG -> JAVADOC_TAG
     *  |--SEE_LITERAL -> @see
     *  |--WS ->
     *  |--REFERENCE -> REFERENCE
     *  |   |--PACKAGE_CLASS -> org.apache.utils.Lists.Comparator
     *  |   |--HASH -> #
     *  |   |--MEMBER -> compare
     *  |   `--PARAMETERS -> PARAMETERS
     *  |       |--LEFT_BRACE -> (
     *  |       |--ARGUMENT -> Object
     *  |       `--RIGHT_BRACE -> )
     *  |--NEWLINE -> \r\n
     *  `--WS ->
     * }
     * </pre>
     */
    public static final int PACKAGE_CLASS = JavadocParser.PACKAGE_CLASS;

    /**
     * Hash character in {@link #REFERENCE}.
     * Hash character is used before specifying a class member.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @see org.apache.utils.Lists.Comparator#compare(Object)}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     * JAVADOC_TAG -> JAVADOC_TAG
     *  |--SEE_LITERAL -> @see
     *  |--WS ->
     *  |--REFERENCE -> REFERENCE
     *      |--PACKAGE_CLASS -> org.apache.utils.Lists.Comparator
     *      |--HASH -> #
     *      |--MEMBER -> compare
     *      `--PARAMETERS -> PARAMETERS
     *          |--LEFT_BRACE -> (
     *          |--ARGUMENT -> Object
     *          `--RIGHT_BRACE -> )
     * }
     * </pre>
     */
    public static final int HASH = JavadocParser.HASH;

    /**
     * A class member in {@link #REFERENCE}.
     * Class member is specified after {@link #HASH} symbol.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @see org.apache.utils.Lists.Comparator#compare(Object)}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     * JAVADOC_TAG -> JAVADOC_TAG
     *  |--SEE_LITERAL -> @see
     *  |--WS ->
     *  |--REFERENCE -> REFERENCE
     *      |--PACKAGE_CLASS -> org.apache.utils.Lists.Comparator
     *      |--HASH -> #
     *      |--MEMBER -> compare
     *      `--PARAMETERS -> PARAMETERS
     *          |--LEFT_BRACE -> (
     *          |--ARGUMENT -> Object
     *          `--RIGHT_BRACE -> )
     * }
     * </pre>
     */
    public static final int MEMBER = JavadocParser.MEMBER;

    /**
     * Left brace in {@link #PARAMETERS} part of {@link #REFERENCE}.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @see #method(Processor, String)}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code JAVADOC_TAG -> JAVADOC_TAG
     *         |--SEE_LITERAL -> @see
     *         |--WS ->
     *         |--REFERENCE -> REFERENCE
     *         |   |--HASH -> #
     *         |   |--MEMBER -> method
     *         |   `--PARAMETERS -> PARAMETERS
     *         |       |--LEFT_BRACE -> (
     *         |       |--ARGUMENT -> Processor
     *         |       |--COMMA -> ,
     *         |       |--WS ->
     *         |       |--ARGUMENT -> String
     *         |       `--RIGHT_BRACE -> )
     * }
     * </pre>
     */
    public static final int LEFT_BRACE = JavadocParser.LEFT_BRACE;

    /**
     * Right brace in {@link #PARAMETERS} part of {@link #REFERENCE}.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @see #method(Processor, String)}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code JAVADOC_TAG -> JAVADOC_TAG
     *         |--SEE_LITERAL -> @see
     *         |--WS ->
     *         |--REFERENCE -> REFERENCE
     *         |   |--HASH -> #
     *         |   |--MEMBER -> method
     *         |   `--PARAMETERS -> PARAMETERS
     *         |       |--LEFT_BRACE -> (
     *         |       |--ARGUMENT -> Processor
     *         |       |--COMMA -> ,
     *         |       |--WS ->
     *         |       |--ARGUMENT -> String
     *         |       `--RIGHT_BRACE -> )
     * }
     * </pre>
     */
    public static final int RIGHT_BRACE = JavadocParser.RIGHT_BRACE;

    /**
     * Argument definition in {@link #PARAMETERS} part of {@link #REFERENCE}.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @see #method(Processor, String)}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code JAVADOC_TAG -> JAVADOC_TAG
     *         |--SEE_LITERAL -> @see
     *         |--WS ->
     *         |--REFERENCE -> REFERENCE
     *         |   |--HASH -> #
     *         |   |--MEMBER -> method
     *         |   `--PARAMETERS -> PARAMETERS
     *         |       |--LEFT_BRACE -> (
     *         |       |--ARGUMENT -> Processor
     *         |       |--COMMA -> ,
     *         |       |--WS ->
     *         |       |--ARGUMENT -> String
     *         |       `--RIGHT_BRACE -> )
     *         `--NEWLINE -> \n
     * }
     * </pre>
     */
    public static final int ARGUMENT = JavadocParser.ARGUMENT;

    /**
     * Comma separator between parameters in {@link #PARAMETERS} part of {@link #REFERENCE}.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @see #method(Processor, String)}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     * JAVADOC_TAG -> JAVADOC_TAG
     *  |--SEE_LITERAL -> @see
     *  |--WS ->
     *  |--REFERENCE -> REFERENCE
     *  |   |--HASH -> #
     *  |   |--MEMBER -> method
     *  |   `--PARAMETERS -> PARAMETERS
     *  |       |--LEFT_BRACE -> (
     *  |       |--ARGUMENT -> Processor
     *  |       |--COMMA -> ,
     *  |       |--WS ->
     *  |       |--ARGUMENT -> String
     *  |       `--RIGHT_BRACE -> )
     *  |--NEWLINE -> \r\n
     *  `--WS ->
     * }
     * </pre>
     *
     * @see #PARAMETERS
     * @see #REFERENCE
     * @see #ARGUMENT
     */
    public static final int COMMA = JavadocParser.COMMA;

    /**
     * Quoted text.
     * One of possible {@code @see} tag arguments.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @see "Spring Framework"}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     * JAVADOC_TAG -> JAVADOC_TAG
     *  |--SEE_LITERAL -> @see
     *  |--WS ->
     *  |--STRING -> "Spring Framework"
     *  |--NEWLINE -> \r\n
     *  `--WS ->
     * }
     * </pre>
     *
     * @see #SEE_LITERAL
     */
    public static final int STRING = JavadocParser.STRING;

    /**
     * Exception class name. First argument in {@link #THROWS_LITERAL @throws} and
     * {@link #EXCEPTION_LITERAL @exception} Javadoc tags.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @throws IOException connection problems}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     * JAVADOC_TAG -> JAVADOC_TAG
     *  |--THROWS_LITERAL -> @throws
     *  |--WS ->
     *  |--CLASS_NAME -> IOException
     *  |--WS ->
     *  `--DESCRIPTION -> DESCRIPTION
     *      |--TEXT -> connection problems
     *      |--NEWLINE -> \r\n
     *      `--TEXT ->
     * }
     * </pre>
     *
     * @see #EXCEPTION_LITERAL
     * @see #THROWS_LITERAL
     */
    public static final int CLASS_NAME = JavadocParser.CLASS_NAME;

    /**
     * First argument in {@link #PARAM_LITERAL @param} Javadoc tag.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @param T The bar.}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   --JAVADOC_TAG -> JAVADOC_TAG
     *      |--PARAM_LITERAL -> @param
     *      |--WS ->
     *      |--PARAMETER_NAME -> T
     *      |--WS ->
     *      `--DESCRIPTION -> DESCRIPTION
     *          |--TEXT -> The bar.
     *          |--NEWLINE -> \r\n
     *          `--TEXT ->
     * }
     * </pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDHJECF">
     *     Oracle Docs</a>
     * @see #PARAM_LITERAL
     */
    public static final int PARAMETER_NAME = JavadocParser.PARAMETER_NAME;

    /**
     * 'exclude' literal.
     * One of three possible {@link #SERIAL_LITERAL @serial} tag arguments.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @serial exclude}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   JAVADOC_TAG -> JAVADOC_TAG
     *    |--SERIAL_LITERAL -> @serial
     *    |--WS ->
     *    `--DESCRIPTION -> DESCRIPTION
     *        |--TEXT -> serialized company name
     *        |--NEWLINE -> \r\n
     *        `--TEXT ->
     * }
     * </pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDHDECF">
     *     Oracle Docs</a>
     * @see #SERIAL_LITERAL
     */
    public static final int LITERAL_EXCLUDE = JavadocParser.LITERAL_EXCLUDE;

    /**
     * 'include' literal.
     * One of three possible {@link #SERIAL_LITERAL @serial} tag arguments.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @serial include}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     * JAVADOC_TAG -> JAVADOC_TAG
     *  |--SERIAL_LITERAL -> @serial
     *  |--WS ->
     *  |--LITERAL_INCLUDE -> include
     *  |--NEWLINE -> \r\n
     *  `--WS ->
     * }
     * </pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDHDECF">
     *     Oracle Docs</a>
     * @see #SERIAL_LITERAL
     */
    public static final int LITERAL_INCLUDE = JavadocParser.LITERAL_INCLUDE;

    /**
     * Field name. First argument of {@link #SERIAL_FIELD_LITERAL @serialField} Javadoc tag.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @serialField counter Integer objects counter}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   --JAVADOC_TAG -> JAVADOC_TAG
     *   |--SERIAL_FIELD_LITERAL -> @serialField
     *   |--WS ->
     *   |--LITERAL_INCLUDE -> include
     *   |--NEWLINE -> \r\n
     *   `--WS ->
     *    `--DESCRIPTION -> DESCRIPTION
     *        |--TEXT -> objects counter
     *        |--NEWLINE -> \r\n
     *        `--TEXT ->
     * }</pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDHDECF">
     *     Oracle Docs</a>
     * @see #SERIAL_FIELD_LITERAL
     */
    public static final int FIELD_NAME = JavadocParser.FIELD_NAME;

    /**
     * Field type. Second argument of {@link #SERIAL_FIELD_LITERAL @serialField} Javadoc tag.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @serialField counter Integer objects counter}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   --JAVADOC_TAG -> JAVADOC_TAG
     *      |--SERIAL_FIELD_LITERAL -> @serialField
     *      |--WS ->
     *      |--FIELD_NAME -> counter
     *      |--WS ->
     *      |--FIELD_TYPE -> Integer
     *      |--WS ->
     *      `--DESCRIPTION -> DESCRIPTION
     *          |--TEXT -> objects counter
     *          |--NEWLINE -> \r\n
     *          `--TEXT ->
     * }
     * </pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#CHDHDECF">
     *     Oracle Docs</a>
     * @see #SERIAL_FIELD_LITERAL
     */
    public static final int FIELD_TYPE = JavadocParser.FIELD_TYPE;

    // ------------------------------------------------------------------------------------------ //
    // -----------------        HTML TAGS          ---------------------------------------------- //
    // ------------------------------------------------------------------------------------------ //

    /**
     * Identifier inside HTML tag: tag name or attribute name.
     */
    public static final int HTML_TAG_NAME = JavadocParser.HTML_TAG_NAME;

    // HTML tag components

    /**
     * Start html tag component: {@code '<'}.
     */
    public static final int START = JavadocParser.START;

    /**
     * Slash html tag component: {@code '/'}.
     */
    public static final int SLASH = JavadocParser.SLASH;

    /**
     * End html tag component: {@code '>'}.
     */
    public static final int END = JavadocParser.END;

    /**
     * Slash close html tag component: {@code '/>'}.
     */
    public static final int SLASH_END = JavadocParser.SLASH_END;

    /**
     * Equals html tag component: {@code '='}.
     */
    public static final int EQUALS = JavadocParser.EQUALS;

    /**
     * Attribute value HTML tag component.
     *
     * <p><b>Example:</b></p>
     *
     * <pre>
     * &lt;tag_name attr_name="attr_value">Content&lt;/tag_name&gt;
     * </pre>
     *
     * <p><b>Tree:</b></p>
     * <pre>{@code
     * JAVADOC -> JAVADOC
     *  |--NEWLINE -> \r\n
     *  |--LEADING_ASTERISK ->  *
     *  |--TEXT ->
     *  |--HTML_ELEMENT -> HTML_ELEMENT
     *  |   `--HTML_TAG -> HTML_TAG
     *  |       |--HTML_ELEMENT_START -> HTML_ELEMENT_START
     *  |       |   |--START -> <
     *  |       |   |--HTML_TAG_NAME -> tag_name
     *  |       |   |--WS ->
     *  |       |   |--ATTRIBUTE -> ATTRIBUTE
     *  |       |   |   |--HTML_TAG_NAME -> attr_name
     *  |       |   |   |--EQUALS -> =
     *  |       |   |   `--ATTR_VALUE -> "attr_value"
     *  |       |   `--END -> >
     *  |       |--TEXT -> Content
     *  |       `--HTML_ELEMENT_END -> HTML_ELEMENT_END
     *  |           |--START -> <
     *  |           |--SLASH -> /
     *  |           |--HTML_TAG_NAME -> tag_name
     *  |           `--END -> >
     * }</pre>
     */
    public static final int ATTR_VALUE = JavadocParser.ATTR_VALUE;

    /////////////////////// HTML TAGS WITH OPTIONAL END TAG /////////////////////////////////////

    /**
     * Paragraph tag name.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code <p>Paragraph Tag.</p>}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *      `--HTML_ELEMENT -> HTML_ELEMENT
     *          `--PARAGRAPH -> PARAGRAPH
     *              |--P_TAG_START -> P_TAG_START
     *              |   |--START -> <
     *              |   |--P_HTML_TAG_NAME -> p
     *              |   `--END -> >
     *              |--TEXT -> Paragraph Tag.
     *              `--P_TAG_END -> P_TAG_END
     *                  |--START -> <
     *                  |--SLASH -> /
     *                  |--P_HTML_TAG_NAME -> p
     *                  `--END -> >
     * }
     * </pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#JSSOR647">
     *     Comments are written in HTML</a>
     * @see #P_HTML_TAG_NAME
     */
    public static final int P_HTML_TAG_NAME = JavadocParser.P_HTML_TAG_NAME;

    /**
     * List item tag name.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     *  <ol>
     *    <li>banana</li>
     *  </ol>
     *  }</pre>
     *  <b>Tree:</b>
     *  <pre>
     *  {@code
     *   HTML_ELEMENT -> HTML_ELEMENT
     *    `--HTML_TAG -> HTML_TAG
     *       |--HTML_ELEMENT_START -> HTML_ELEMENT_START
     *       |   |--START -> <
     *       |   |--HTML_TAG_NAME -> ol
     *       |   `--END -> >
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       |--HTML_ELEMENT -> HTML_ELEMENT
     *       |   `--LI -> LI
     *       |       |--LI_TAG_START -> LI_TAG_START
     *       |       |   |--START -> <
     *       |       |   |--LI_HTML_TAG_NAME -> li
     *       |       |   `--END -> >
     *       |       |--TEXT -> banana
     *       |       `--LI_TAG_END -> LI_TAG_END
     *       |           |--START -> <
     *       |           |--SLASH -> /
     *       |           |--LI_HTML_TAG_NAME -> li
     *       |           `--END -> >
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       `--HTML_ELEMENT_END -> HTML_ELEMENT_END
     *           |--START -> <
     *           |--SLASH -> /
     *           |--HTML_TAG_NAME -> ol
     *           `--END -> >
     *  }
     *  </pre>
     *
     * @see
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#JSSOR647">
     *     comments are written in HTML</a>
     * @see #LI_HTML_TAG_NAME
     */
    public static final int LI_HTML_TAG_NAME = JavadocParser.LI_HTML_TAG_NAME;

    /**
     * Table row tag name.
     *
     *  <p><b>Example:</b></p>
     *  <pre>{@code
     *  <table>
     *     <tr>Table Row</tr>
     *  </table>
     *  }</pre>
     *  <b>Tree:</b>
     *  <pre>
     *  {@code
     *  HTML_ELEMENT -> HTML_ELEMENT
     *   `--HTML_TAG -> HTML_TAG
     *       |--HTML_ELEMENT_START -> HTML_ELEMENT_START
     *       |   |--START -> <
     *       |   |--HTML_TAG_NAME -> table
     *       |   `--END -> >
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       |--HTML_ELEMENT -> HTML_ELEMENT
     *       |   `--TR -> TR
     *       |       |--TR_TAG_START -> TR_TAG_START
     *       |       |   |--START -> <
     *       |       |   |--TR_HTML_TAG_NAME -> tr
     *       |       |   `--END -> >
     *       |       |--TEXT -> Table Row
     *       |       `--TR_TAG_END -> TR_TAG_END
     *       |           |--START -> <
     *       |           |--SLASH -> /
     *       |           |--TR_HTML_TAG_NAME -> tr
     *       |           `--END -> >
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       `--HTML_ELEMENT_END -> HTML_ELEMENT_END
     *           |--START -> <
     *           |--SLASH -> /
     *           |--HTML_TAG_NAME -> table
     *           `--END -> >
     *  }
     *  </pre>
     *
     *  @see
     *  <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#JSSOR647">
     *      comments are written in HTML</a>
     *  @see #TR_HTML_TAG_NAME
     */
    public static final int TR_HTML_TAG_NAME = JavadocParser.TR_HTML_TAG_NAME;

    /**
     * Table cell tag name.
     *
     *  <p><b>Example:</b></p>
     *  <pre>{@code
     *  <tr>
     *     <td>Cell 1</td>
     *  </tr>
     *  }</pre>
     *  <b>Tree:</b>
     *  <pre>
     *  {@code
     *  HTML_ELEMENT -> HTML_ELEMENT
     *   `--TR -> TR
     *       |--TR_TAG_START -> TR_TAG_START
     *       |   |--START -> <
     *       |   |--TR_HTML_TAG_NAME -> tr
     *       |   `--END -> >
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       |--TD -> TD
     *       |   |--TD_TAG_START -> TD_TAG_START
     *       |   |   |--START -> <
     *       |   |   |--TD_HTML_TAG_NAME -> td
     *       |   |   `--END -> >
     *       |   |--TEXT -> Cell 1
     *       |   `--TD_TAG_END -> TD_TAG_END
     *       |       |--START -> <
     *       |       |--SLASH -> /
     *       |       |--TD_HTML_TAG_NAME -> td
     *       |       `--END -> >
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       `--TR_TAG_END -> TR_TAG_END
     *           |--START -> <
     *           |--SLASH -> /
     *           |--TR_HTML_TAG_NAME -> tr
     *           `--END -> >
     *  }
     *  </pre>
     *
     *  @see
     *  <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#JSSOR647">
     *      comments are written in HTML</a>
     *  @see #TD_HTML_TAG_NAME
     */
    public static final int TD_HTML_TAG_NAME = JavadocParser.TD_HTML_TAG_NAME;

    /**
     * Table header cell tag name.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * <tr>
     *     <th>Table Head</th>
     * </tr>
     * }</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *  HTML_ELEMENT -> HTML_ELEMENT
     *   `--TR -> TR
     *       |--TR_TAG_START -> TR_TAG_START
     *       |   |--START -> <
     *       |   |--TR_HTML_TAG_NAME -> tr
     *       |   `--END -> >
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       |--TH -> TH
     *       |   |--TH_TAG_START -> TH_TAG_START
     *       |   |   |--START -> <
     *       |   |   |--TH_HTML_TAG_NAME -> th
     *       |   |   `--END -> >
     *       |   |--TEXT -> Table Head
     *       |   `--TH_TAG_END -> TH_TAG_END
     *       |       |--START -> <
     *       |       |--SLASH -> /
     *       |       |--TH_HTML_TAG_NAME -> th
     *       |       `--END -> >
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       `--TR_TAG_END -> TR_TAG_END
     *           |--START -> <
     *           |--SLASH -> /
     *           |--TR_HTML_TAG_NAME -> tr
     *           `--END -> >
     *  }
     *  </pre>
     *
     *  @see
     *  <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#JSSOR647">
     *      comments are written in HTML</a>
     *  @see #TH_HTML_TAG_NAME
     */
    public static final int TH_HTML_TAG_NAME = JavadocParser.TH_HTML_TAG_NAME;

    /**
     * Body tag name.
     *
     *  <p><b>Example:</b></p>
     *  <pre>
     *  &lt;body&gt;
     *     &lt;p&gt;Body Content&lt;/p&gt;
     *  &lt;/body&gt;
     *  </pre>
     *  <b>Tree:</b>
     *  <pre>
     *  {@code
     *  HTML_ELEMENT -> HTML_ELEMENT
     *   `--BODY -> BODY
     *       |--BODY_TAG_START -> BODY_TAG_START
     *       |   |--START -> <
     *       |   |--BODY_HTML_TAG_NAME -> body
     *       |   `--END -> >
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       |--PARAGRAPH -> PARAGRAPH
     *       |   |--P_TAG_START -> P_TAG_START
     *       |   |   |--START -> <
     *       |   |   |--P_HTML_TAG_NAME -> p
     *       |   |   `--END -> >
     *       |   |--TEXT -> Body Content
     *       |   `--P_TAG_END -> P_TAG_END
     *       |       |--START -> <
     *       |       |--SLASH -> /
     *       |       |--P_HTML_TAG_NAME -> p
     *       |       `--END -> >
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       `--BODY_TAG_END -> BODY_TAG_END
     *           |--START -> <
     *           |--SLASH -> /
     *           |--BODY_HTML_TAG_NAME -> body
     *           `--END -> >
     *  }
     *  </pre>
     *
     *  @see
     *  <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#JSSOR647">
     *      comments are written in HTML</a>
     *  @see #BODY_HTML_TAG_NAME
     */
    public static final int BODY_HTML_TAG_NAME = JavadocParser.BODY_HTML_TAG_NAME;

    /**
     * Colgroup tag name.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code <colgroup><col span="2"></colgroup>}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   |--HTML_ELEMENT -> HTML_ELEMENT
     *   |   `--COLGROUP -> COLGROUP
     *   |       |--COLGROUP_TAG_START -> COLGROUP_TAG_START
     *   |       |   |--START -> <
     *   |       |   |--COLGROUP_HTML_TAG_NAME -> colgroup
     *   |       |   `--END -> >
     *   |       |--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *   |       |   `--COL_TAG -> COL_TAG
     *   |       |       |--START -> <
     *   |       |       |--COL_HTML_TAG_NAME -> col
     *   |       |       |--WS ->
     *   |       |       |--ATTRIBUTE -> ATTRIBUTE
     *   |       |       |   |--HTML_TAG_NAME -> span
     *   |       |       |   |--EQUALS -> =
     *   |       |       |   `--ATTR_VALUE -> "2"
     *   |       |       `--END -> >
     *   |       `--COLGROUP_TAG_END -> COLGROUP_TAG_END
     *   |           |--START -> <
     *   |           |--SLASH -> /
     *   |           |--COLGROUP_HTML_TAG_NAME -> colgroup
     *   |           `--END -> >
     *   |--TEXT ->
     *   `--EOF -> <EOF>
     * }
     * </pre>
     *
     * @see #COLGROUP_HTML_TAG_NAME
     */
    public static final int COLGROUP_HTML_TAG_NAME = JavadocParser.COLGROUP_HTML_TAG_NAME;

    /**
     * Description of a term tag name.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * <dl>
     *     <dt>Java</dt>
     *     <dd>A high-level programming language.</dd>
     * </dl>
     * }</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *  HTML_ELEMENT -> HTML_ELEMENT
     *   `--HTML_TAG -> HTML_TAG
     *       |--HTML_ELEMENT_START -> HTML_ELEMENT_START
     *       |   |--START -> <
     *       |   |--HTML_TAG_NAME -> dl
     *       |   `--END -> >
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       |--HTML_ELEMENT -> HTML_ELEMENT
     *       |   `--DT -> DT
     *       |       |--DT_TAG_START -> DT_TAG_START
     *       |       |   |--START -> <
     *       |       |   |--DT_HTML_TAG_NAME -> dt
     *       |       |   `--END -> >
     *       |       |--TEXT -> Java
     *       |       `--DT_TAG_END -> DT_TAG_END
     *       |           |--START -> <
     *       |           |--SLASH -> /
     *       |           |--DT_HTML_TAG_NAME -> dt
     *       |           `--END -> >
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       |--HTML_ELEMENT -> HTML_ELEMENT
     *       |   `--DD -> DD
     *       |       |--DD_TAG_START -> DD_TAG_START
     *       |       |   |--START -> <
     *       |       |   |--DD_HTML_TAG_NAME -> dd
     *       |       |   `--END -> >
     *       |       |--TEXT -> A high-level programming language.
     *       |       `--DD_TAG_END -> DD_TAG_END
     *       |           |--START -> <
     *       |           |--SLASH -> /
     *       |           |--DD_HTML_TAG_NAME -> dd
     *       |           `--END -> >
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       `--HTML_ELEMENT_END -> HTML_ELEMENT_END
     *           |--START -> <
     *           |--SLASH -> /
     *           |--HTML_TAG_NAME -> dl
     *           `--END -> >
     * }
     * </pre>
     *
     * @see #DD_HTML_TAG_NAME
     */
    public static final int DD_HTML_TAG_NAME = JavadocParser.DD_HTML_TAG_NAME;

    /**
     * Description term tag name.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * <dl>
     *     <dt>Java</dt>
     * </dl>
     * }</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *  HTML_ELEMENT -> HTML_ELEMENT
     *   `--HTML_TAG -> HTML_TAG
     *       |--HTML_ELEMENT_START -> HTML_ELEMENT_START
     *       |   |--START -> <
     *       |   |--HTML_TAG_NAME -> dl
     *       |   `--END -> >
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       |--HTML_ELEMENT -> HTML_ELEMENT
     *       |   `--DT -> DT
     *       |       |--DT_TAG_START -> DT_TAG_START
     *       |       |   |--START -> <
     *       |       |   |--DT_HTML_TAG_NAME -> dt
     *       |       |   `--END -> >
     *       |       |--TEXT -> Java
     *       |       `--DT_TAG_END -> DT_TAG_END
     *       |           |--START -> <
     *       |           |--SLASH -> /
     *       |           |--DT_HTML_TAG_NAME -> dt
     *       |           `--END -> >
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       `--HTML_ELEMENT_END -> HTML_ELEMENT_END
     *           |--START -> <
     *           |--SLASH -> /
     *           |--HTML_TAG_NAME -> dl
     *           `--END -> >
     * }
     * </pre>
     *
     * @see #DT_HTML_TAG_NAME
     */
    public static final int DT_HTML_TAG_NAME = JavadocParser.DT_HTML_TAG_NAME;

    /**
     * Head tag name.
     *
     * <p><b>Example:</b></p>
     * <pre>
     * &lt;head&gt;
     *   &lt;title&gt;Page Title&lt;/title&gt;
     *   &lt;meta charset="UTF-8"&gt;
     * &lt;/head&gt;
     * </pre>
     * <b>Tree:</b>
     * <pre>{@code
     *  HEAD -> HEAD
     *   |--HEAD_TAG_START -> HEAD_TAG_START
     *   |   |--START -> <
     *   |   |--HEAD_HTML_TAG_NAME -> head
     *   |   `--END -> >
     *   |--NEWLINE -> \r\n
     *   |--LEADING_ASTERISK ->  *
     *   |--TEXT ->
     *   |--HTML_TAG -> HTML_TAG
     *   |   |--HTML_ELEMENT_START -> HTML_ELEMENT_START
     *   |   |   |--START -> <
     *   |   |   |--HTML_TAG_NAME -> title
     *   |   |   `--END -> >
     *   |   |--TEXT -> Page Title
     *   |   `--HTML_ELEMENT_END -> HTML_ELEMENT_END
     *   |       |--START -> <
     *   |       |--SLASH -> /
     *   |       |--HTML_TAG_NAME -> title
     *   |       `--END -> >
     *   |--NEWLINE -> \r\n
     *   |--LEADING_ASTERISK ->  *
     *   |--TEXT ->
     *   |--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *   |   `--META_TAG -> META_TAG
     *   |       |--START -> <
     *   |       |--META_HTML_TAG_NAME -> meta
     *   |       |--WS ->
     *   |       |--ATTRIBUTE -> ATTRIBUTE
     *   |       |   |--HTML_TAG_NAME -> charset
     *   |       |   |--EQUALS -> =
     *   |       |   `--ATTR_VALUE -> "UTF-8"
     *   |       `--END -> >
     *   |--NEWLINE -> \r\n
     *   |--LEADING_ASTERISK ->  *
     *   |--TEXT ->
     *   `--HEAD_TAG_END -> HEAD_TAG_END
     *       |--START -> <
     *       |--SLASH -> /
     *       |--HEAD_HTML_TAG_NAME -> head
     *       `--END -> >
     * }
     * </pre>
     *
     * @see #HEAD_HTML_TAG_NAME
     */
    public static final int HEAD_HTML_TAG_NAME = JavadocParser.HEAD_HTML_TAG_NAME;

    /** Html tag name. */
    public static final int HTML_HTML_TAG_NAME = JavadocParser.HTML_HTML_TAG_NAME;

    /**
     * Option tag name.
     *
     * <p><b>Example:</b></p>
     * <pre>&lt;option value="yes"&gt;Yes&lt;/option&gt;</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     * JAVADOC -> JAVADOC
     * |--NEWLINE -> \r\n
     * |--LEADING_ASTERISK ->  *
     * |--HTML_ELEMENT -> HTML_ELEMENT
     * |   `--OPTION -> OPTION
     * |       |--OPTION_TAG_START -> OPTION_TAG_START
     * |       |   |--START -> <
     * |       |   |--OPTION_HTML_TAG_NAME -> option
     * |       |   |--WS ->
     * |       |   |--ATTRIBUTE -> ATTRIBUTE
     * |       |   |   |--HTML_TAG_NAME -> value
     * |       |   |   |--EQUALS -> =
     * |       |   |   `--ATTR_VALUE -> "yes"
     * |       |   `--END -> >
     * |       |--TEXT -> Yes
     * |       `--OPTION_TAG_END -> OPTION_TAG_END
     * |           |--START -> <
     * |           |--SLASH -> /
     * |           |--OPTION_HTML_TAG_NAME -> option
     * |           `--END -> >
     * |--NEWLINE -> \r\n
     * |--TEXT ->
     * |--EOF -> <EOF>
     * }
     * </pre>
     */
    public static final int OPTION_HTML_TAG_NAME = JavadocParser.OPTION_HTML_TAG_NAME;

    /**
     * Table body tag name.
     *
     * <p><b>Example:</b></p>
     * <pre>
     * &lt;tbody&gt;
     * &lt;tr&gt;
     * &lt;td&gt;Row1&lt;/td&gt;
     * &lt;/tr&gt;
     * &lt;/tbody&gt;
     * </pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     * JAVADOC -> JAVADOC
     * |--NEWLINE -> \n
     * |--LEADING_ASTERISK ->  *
     * |--TEXT ->
     * |--HTML_ELEMENT -> HTML_ELEMENT
     * |   `--TBODY -> TBODY
     * |       |--TBODY_TAG_START -> TBODY_TAG_START
     * |       |   |--START -> <
     * |       |   |--TBODY_HTML_TAG_NAME -> tbody
     * |       |   `--END -> >
     * |       |--NEWLINE -> \n
     * |       |--LEADING_ASTERISK ->  *
     * |       |--TEXT ->
     * |       |--TR -> TR
     * |       |   |--TR_TAG_START -> TR_TAG_START
     * |       |   |   |--START -> <
     * |       |   |   |--TR_HTML_TAG_NAME -> tr
     * |       |   |   `--END -> >
     * |       |   |--NEWLINE -> \n
     * |       |   |--LEADING_ASTERISK ->  *
     * |       |   |--TEXT ->
     * |       |   |--TD -> TD
     * |       |   |   |--TD_TAG_START -> TD_TAG_START
     * |       |   |   |   |--START -> <
     * |       |   |   |   |--TD_HTML_TAG_NAME -> td
     * |       |   |   |   `--END -> >
     * |       |   |   |--TEXT -> Row1
     * |       |   |   `--TD_TAG_END -> TD_TAG_END
     * |       |   |       |--START -> <
     * |       |   |       |--SLASH -> /
     * |       |   |       |--TD_HTML_TAG_NAME -> td
     * |       |   |       `--END -> >
     * |       |   |--NEWLINE -> \n
     * |       |   |--LEADING_ASTERISK ->  *
     * |       |   |--TEXT ->
     * |       |   `--TR_TAG_END -> TR_TAG_END
     * |       |       |--START -> <
     * |       |       |--SLASH -> /
     * |       |       |--TR_HTML_TAG_NAME -> tr
     * |       |       `--END -> >
     * |       |--NEWLINE -> \n
     * |       |--LEADING_ASTERISK ->  *
     * |       |--TEXT ->
     * |       `--TBODY_TAG_END -> TBODY_TAG_END
     * |           |--START -> <
     * |           |--SLASH -> /
     * |           |--TBODY_HTML_TAG_NAME -> tbody
     * |           `--END -> >
     * |--NEWLINE -> \n
     * |--TEXT ->
     * `--EOF -> <EOF>
     * }
     * </pre>
     */
    public static final int TBODY_HTML_TAG_NAME = JavadocParser.TBODY_HTML_TAG_NAME;

    /** Table foot tag name. */
    public static final int TFOOT_HTML_TAG_NAME = JavadocParser.TFOOT_HTML_TAG_NAME;

    /**
     * Table head tag name.
     *
     * <p><b>Example:</b></p>
     * <pre>
     * &lt;thead&gt;
     * &lt;tr&gt;
     * &lt;th&gt;Header&lt;/th&gt;
     * &lt;/tr&gt;
     * &lt;/thead&gt;
     * </pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     * JAVADOC -> JAVADOC
     * |--NEWLINE -> \n
     * |--LEADING_ASTERISK ->  *
     * |--TEXT ->
     * |--HTML_ELEMENT -> HTML_ELEMENT
     * |   `--THEAD -> THEAD
     * |       |--THEAD_TAG_START -> THEAD_TAG_START
     * |       |   |--START -> <
     * |       |   |--THEAD_HTML_TAG_NAME -> thead
     * |       |   `--END -> >
     * |       |--TR -> TR
     * |       |   |--TR_TAG_START -> TR_TAG_START
     * |       |   |   |--START -> <
     * |       |   |   |--TR_HTML_TAG_NAME -> tr
     * |       |   |   `--END -> >
     * |       |   |--TH -> TH
     * |       |   |   |--TH_TAG_START -> TH_TAG_START
     * |       |   |   |   |--START -> <
     * |       |   |   |   |--TH_HTML_TAG_NAME -> th
     * |       |   |   |   `--END -> >
     * |       |   |   |--TEXT -> Header
     * |       |   |   `--TH_TAG_END -> TH_TAG_END
     * |       |   |       |--START -> <
     * |       |   |       |--SLASH -> /
     * |       |   |       |--TH_HTML_TAG_NAME -> th
     * |       |   |       `--END -> >
     * |       |   `--TR_TAG_END -> TR_TAG_END
     * |       |       |--START -> <
     * |       |       |--SLASH -> /
     * |       |       |--TR_HTML_TAG_NAME -> tr
     * |       |       `--END -> >
     * |       `--THEAD_TAG_END -> THEAD_TAG_END
     * |           |--START -> <
     * |           |--SLASH -> /
     * |           |--THEAD_HTML_TAG_NAME -> thead
     * |           `--END -> >
     * |--NEWLINE -> \n
     * |--TEXT ->
     * `--EOF -> <EOF>
     * }
     * </pre>
     */
    public static final int THEAD_HTML_TAG_NAME = JavadocParser.THEAD_HTML_TAG_NAME;

    /** `optgroup` tag name. */
    public static final int OPTGROUP_HTML_TAG_NAME = JavadocParser.OPTGROUP_HTML_TAG_NAME;

    /** `rb` tag name. */
    public static final int RB_HTML_TAG_NAME = JavadocParser.RB_HTML_TAG_NAME;

    /** `rt` tag name. */
    public static final int RT_HTML_TAG_NAME = JavadocParser.RT_HTML_TAG_NAME;

    /** `rtc` tag name. */
    public static final int RTC_HTML_TAG_NAME = JavadocParser.RTC_HTML_TAG_NAME;

    /**
     * RP tag name.
     *
     * <p><b>Example:</b></p>
     * <pre>
     * &lt;rp&gt;Fallback&lt;/rp&gt;
     * </pre>
     *
     * <b>Tree:</b>
     * <pre>
     * {@code
     * HTML_ELEMENT -> HTML_ELEMENT
     *     `--RP -> RP
     *         |--RP_TAG_START -> RP_TAG_START
     *         |   |--START -> <
     *         |   |--RP_HTML_TAG_NAME -> rp
     *         |   `--END -> >
     *         |--TEXT -> Fallback
     *         `--RP_TAG_END -> RP_TAG_END
     *             |--START -> <
     *             |--SLASH -> /
     *             |--RP_HTML_TAG_NAME -> rp
     *             `--END -> >
     * }
     * </pre>
     */
    public static final int RP_HTML_TAG_NAME = JavadocParser.RP_HTML_TAG_NAME;

    ///////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////// SINGLETON HTML TAGS  //////////////////////////////////////////////////
    /**
     * Area tag name.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code < area shape="rect" >}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   JAVADOC -> JAVADOC
     *        |--NEWLINE -> \n
     *        |--LEADING_ASTERISK ->  *
     *        |--TEXT ->
     *        |--HTML_ELEMENT -> HTML_ELEMENT
     *        |   `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *        |       `--AREA_TAG -> AREA_TAG
     *        |           |--START -> <
     *        |           |--AREA_HTML_TAG_NAME -> area
     *        |           |--WS ->
     *        |           |--ATTRIBUTE -> ATTRIBUTE
     *        |           |   |--HTML_TAG_NAME -> shape
     *        |           |   |--EQUALS -> =
     *        |           |   `--ATTR_VALUE -> "rect"
     *        |           `--END -> >
     *        |--TEXT ->
     *        |--NEWLINE -> \n
     *        |--TEXT ->
     * }
     * </pre>
     */

    public static final int AREA_HTML_TAG_NAME = JavadocParser.AREA_HTML_TAG_NAME;

    /** Base tag name. */
    public static final int BASE_HTML_TAG_NAME = JavadocParser.BASE_HTML_TAG_NAME;

    /** Basefont tag name. */
    public static final int BASEFONT_HTML_TAG_NAME = JavadocParser.BASEFONT_HTML_TAG_NAME;

    /**
     * Br tag name.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code text before break < br > text after break}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   `--JAVADOC -> JAVADOC
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->  text before break
     *       |--HTML_ELEMENT -> HTML_ELEMENT
     *       |   `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *       |       `--BR_TAG -> BR_TAG
     *       |           |--START -> <
     *       |           |--BR_HTML_TAG_NAME -> br
     *       |           `--END -> >
     *       |--TEXT ->  text after break
     *       |--NEWLINE -> \r\n
     *       |--TEXT ->
     * }
     * </pre>
     */

    public static final int BR_HTML_TAG_NAME = JavadocParser.BR_HTML_TAG_NAME;

    /** Col tag name. */
    public static final int COL_HTML_TAG_NAME = JavadocParser.COL_HTML_TAG_NAME;

    /**
     * Frame tag name.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code <frame src="frame_a.html">}</pre>
     *
     * <p><b>Tree:</b></p>
     * <pre>
     * {@code
     * |--HTML_ELEMENT -> HTML_ELEMENT
     * |   `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     * |       `--FRAME_TAG -> FRAME_TAG
     * |           |--START -> <
     * |           |--FRAME_HTML_TAG_NAME -> frame
     * |           |--WS ->
     * |           |--ATTRIBUTE -> ATTRIBUTE
     * |           |   |--HTML_TAG_NAME -> src
     * |           |   |--EQUALS -> =
     * |           |   `--ATTR_VALUE -> "frame_a.html"
     * |           `--END -> >
     * }
     * </pre>
     */
    public static final int FRAME_HTML_TAG_NAME = JavadocParser.FRAME_HTML_TAG_NAME;

    /**
     * Hr tag name.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code text before horizontal rule < hr > text after horizontal rule}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *  |--TEXT ->  text before horizontal rule
     *  |--HTML_ELEMENT -> HTML_ELEMENT
     *  |   `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *  |       `--HR_TAG -> HR_TAG
     *  |           |--START -> <
     *  |           |--HR_HTML_TAG_NAME -> hr
     *  |           `--END -> >
     *  |--TEXT ->  text after horizontal rule
     *  |--NEWLINE -> \r\n
     * }
     * </pre>
     */

    public static final int HR_HTML_TAG_NAME = JavadocParser.HR_HTML_TAG_NAME;

    /** Img tag name. */
    public static final int IMG_HTML_TAG_NAME = JavadocParser.IMG_HTML_TAG_NAME;

    /**
     * Input tag name.
     *
     *  <p><b>Example:</b></p>
     *  <pre>{@code <input name="Name" type="text" placeholder="Enter your name"/>}</pre>
     *  <b>Tree:</b>
     *  <pre>
     *  {@code
     *    HTML_ELEMENT -> HTML_ELEMENT
     *       `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *         `--INPUT_TAG -> INPUT_TAG
     *             |--START -> <
     *             |--INPUT_HTML_TAG_NAME -> input
     *             |--WS ->
     *             |--ATTRIBUTE -> ATTRIBUTE
     *             |   |--HTML_TAG_NAME -> name
     *             |   |--EQUALS -> =
     *             |   `--ATTR_VALUE -> "Name"
     *             |--WS ->
     *             |--ATTRIBUTE -> ATTRIBUTE
     *             |   |--HTML_TAG_NAME -> type
     *             |   |--EQUALS -> =
     *             |   `--ATTR_VALUE -> "text"
     *             |--WS ->
     *             |--ATTRIBUTE -> ATTRIBUTE
     *             |   |--HTML_TAG_NAME -> placeholder
     *             |   |--EQUALS -> =
     *             |   `--ATTR_VALUE -> "Enter your name"
     *              `--SLASH_END -> />
     *  }
     *  </pre>
     *
     *   @see
     *   <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#JSSOR647">
     *      comments are written in HTML</a>
     *     @see #INPUT_HTML_TAG_NAME
     */
    public static final int INPUT_HTML_TAG_NAME = JavadocParser.INPUT_HTML_TAG_NAME;

    /** Isindex tag name. */
    public static final int ISINDEX_HTML_TAG_NAME = JavadocParser.ISINDEX_HTML_TAG_NAME;

    /**
     *  Link tag name.
     *
     *  <p><b>Example:</b></p>
     *  <pre>{@code <link rel="stylesheet" href="Style.css">}</pre>
     *  <b>Tree:</b>
     *  <pre>
     *  {@code
     *  HTML_ELEMENT -> HTML_ELEMENT
     *  `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *      `--LINK_TAG -> LINK_TAG
     *          |--START -> <
     *          |--LINK_HTML_TAG_NAME -> link
     *          |--WS ->
     *          |--ATTRIBUTE -> ATTRIBUTE
     *          |   |--HTML_TAG_NAME -> rel
     *          |   |--EQUALS -> =
     *          |   `--ATTR_VALUE -> "stylesheet"
     *          |--WS ->
     *          |--ATTRIBUTE -> ATTRIBUTE
     *          |   |--HTML_TAG_NAME -> href
     *          |   |--EQUALS -> =
     *          |   `--ATTR_VALUE -> "Style.css"
     *          `--END -> >
     *  }
     *  </pre>
     *
     *  @see
     *  <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#JSSOR647">
     *      comments are written in HTML</a>
     *  @see #LINK_HTML_TAG_NAME
     */
    public static final int LINK_HTML_TAG_NAME = JavadocParser.LINK_HTML_TAG_NAME;

    /**
     *  Meta tag name.
     *
     *   <p><b>Example:</b></p>
     *   <pre>{@code <meta charset="UTF-8">}</pre>
     *   <b>Tree:</b>
     *   <pre>
     *   {@code
     *   HTML_ELEMENT -> HTML_ELEMENT
     *    `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *       `--META_TAG -> META_TAG
     *          |--START -> <
     *          |--META_HTML_TAG_NAME -> meta
     *          |--WS ->
     *          |--ATTRIBUTE -> ATTRIBUTE
     *          |   |--HTML_TAG_NAME -> charset
     *          |   |--EQUALS -> =
     *          |   `--ATTR_VALUE -> "UTF-8"
     *           `--END -> >
     *  }
     * </pre>
     */
    public static final int META_HTML_TAG_NAME = JavadocParser.META_HTML_TAG_NAME;

    /** Param tag name. */
    public static final int PARAM_HTML_TAG_NAME = JavadocParser.PARAM_HTML_TAG_NAME;
    /** "embed" tag name. */
    public static final int EMBED_HTML_TAG_NAME = JavadocParser.EMBED_HTML_TAG_NAME;
    /** "keygen" tag name. */
    public static final int KEYGEN_HTML_TAG_NAME = JavadocParser.KEYGEN_HTML_TAG_NAME;
    /** "source" tag name. */
    public static final int SOURCE_HTML_TAG_NAME = JavadocParser.SOURCE_HTML_TAG_NAME;
    /** "track" tag name. */
    public static final int TRACK_HTML_TAG_NAME = JavadocParser.TRACK_HTML_TAG_NAME;
    /** "wbr" tag name. */
    public static final int WBR_HTML_TAG_NAME = JavadocParser.WBR_HTML_TAG_NAME;
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * HTML comment start symbol  '&lt;!--'.
     *
     * <p><b>Example:</b></p>
     * <pre>
     * &lt;!--
     * This is an HTML multi-line comment:
     * This is another comment
     * -->
     * </pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     * HTML_COMMENT -> HTML_COMMENT
     *    |--HTML_COMMENT_START -> <!--
     *    |--NEWLINE -> \n
     *    |--LEADING_ASTERISK ->  *
     *    |--TEXT ->  This is an HTML multi-line comment:
     *    |--NEWLINE -> \n
     *    |--LEADING_ASTERISK ->  *
     *    |--TEXT ->  This is another comment
     *    |--NEWLINE -> \n
     *    |--LEADING_ASTERISK ->  *
     *    |--TEXT ->
     *    `--HTML_COMMENT_END -> -->
     * }
     * </pre>
     */
    public static final int HTML_COMMENT_START = JavadocParser.HTML_COMMENT_START;

    /**
     * HTML comment end symbol {@code '-->'} .
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * <!--
     * This is an HTML multi-line comment:
     * This is another comment
     * -->
     * }</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     * HTML_COMMENT -> HTML_COMMENT
     *    |--HTML_COMMENT_START -> <!--
     *    |--NEWLINE -> \n
     *    |--LEADING_ASTERISK ->  *
     *    |--TEXT ->  This is an HTML multi-line comment:
     *    |--NEWLINE -> \n
     *    |--LEADING_ASTERISK ->  *
     *    |--TEXT ->  This is another comment
     *    |--NEWLINE -> \n
     *    |--LEADING_ASTERISK ->  *
     *    |--TEXT ->
     *    `--HTML_COMMENT_END -> -->
     * }
     * </pre>
     */
    public static final int HTML_COMMENT_END = JavadocParser.HTML_COMMENT_END;

    // ------------------------------------------------------------------------------------------ //
    // -----------------        OTHER          -------------------------------------------------- //
    // ------------------------------------------------------------------------------------------ //

    /** Leading asterisk. */
    public static final int LEADING_ASTERISK = JavadocParser.LEADING_ASTERISK;

    /**
     * Newline symbol - '\n'.
     */
    public static final int NEWLINE = JavadocParser.NEWLINE;

    /**
     * Any other symbol.
     */
    public static final int CHAR = JavadocParser.CHAR;

    /**
     * Whitespace or tab ('\t') symbol.
     */
    public static final int WS = JavadocParser.WS;

    /**
     * End Of File symbol. Copied from
     * {@link org.antlr.v4.runtime.Recognizer#EOF} to avoid ANTLR dependency in
     * API.
     */
    public static final int EOF = -1;

    // ------------------------------------------------------------------------------------------ //
    // ------ JAVADOC TAGS DEPENDING ON RULE TYPES OFFSET --------------------------------------- //
    // ------------------------------------------------------------------------------------------ //

    /**
     * Rule types offset.
     * RULE_TYPES_OFFSET constant is used to split lexer tokens types and parser rules types.
     * We need unique numbers for all tokens,
     * ANTLR do not need this and that is why these types are mixed by used values.
     * All values we can take a look at
     * target/generated-sources/antlr/com/puppycrawl/tools/checkstyle/grammar/javadoc/JavadocParser.java
     * For example: LEADING_ASTERISK=1 and RULE_htmlElement = 1.
     * RULE_TYPES_OFFSET required to shift parser rules,
     * to let them not overlap with types that have prefix "RULE_".
     */
    private static final int RULE_TYPES_OFFSET = 10000;

    /**
     * Root node of any Javadoc comment.
     * Last child is always {@link #EOF}.
     *
     * <p><b>Tree for example:</b></p>
     * <pre>{@code
     * JAVADOC -> JAVADOC
     *  |--NEWLINE -> \n
     *  |--LEADING_ASTERISK ->  *
     *  |--WS ->
     *  |--JAVADOC_TAG -> JAVADOC_TAG
     *  |   |--PARAM_LITERAL -> @param
     *  |   |--WS ->
     *  |   |--PARAMETER_NAME -> T
     *  |   |--WS ->
     *  |   `--DESCRIPTION -> DESCRIPTION
     *  |       |--TEXT -> The bar.
     *  |       |--NEWLINE -> \n
     *  |       `--TEXT ->
     *  `--EOF -> <EOF>
     * }</pre>
     */
    public static final int JAVADOC = JavadocParser.RULE_javadoc + RULE_TYPES_OFFSET;

    /**
     * Javadoc tag.
     *
     * <p>Type of Javadoc tag is resolved by literal node that is first child of this node.</p>
     *
     * <p>As literal could be:</p>
     * <ul>
     * <li>{@link #RETURN_LITERAL}</li>
     * <li>{@link #DEPRECATED_LITERAL}</li>
     * <li>{@link #SINCE_LITERAL}</li>
     * <li>{@link #SERIAL_DATA_LITERAL}</li>
     * <li>{@link #SERIAL_FIELD_LITERAL}</li>
     * <li>{@link #PARAM_LITERAL}</li>
     * <li>{@link #SEE_LITERAL}</li>
     * <li>{@link #SERIAL_LITERAL}</li>
     * <li>{@link #VERSION_LITERAL}</li>
     * <li>{@link #EXCEPTION_LITERAL}</li>
     * <li>{@link #THROWS_LITERAL}</li>
     * <li>{@link #AUTHOR_LITERAL}</li>
     * <li>or {@link #CUSTOM_NAME} if it is custom Javadoc tag.</li>
     * </ul>
     *
     * <p><b>Example</b></p>
     * <pre>{@code @param T The bar.}</pre>
     * <b>Tree</b>
     * <pre>{@code
     * JAVADOC_TAG -> JAVADOC_TAG
     *  |--PARAM_LITERAL -> @param
     *  |--WS ->
     *  |--PARAMETER_NAME -> T
     *  |--WS ->
     *  `--DESCRIPTION -> DESCRIPTION
     *      |--TEXT -> The bar.
     *      |--NEWLINE -> \r\n
     *      `--TEXT ->
     * }</pre>
     */

    public static final int JAVADOC_TAG = JavadocParser.RULE_javadocTag + RULE_TYPES_OFFSET;
    /**
     * Javadoc inline tag.
     *
     * <p>Type of Javadoc inline tag is resolved by literal node that is second child of this node.
     * First child is always {@link #JAVADOC_INLINE_TAG_START} and last node is always
     * {@link #JAVADOC_INLINE_TAG_END}.</p>
     *
     * <p>As literal could be:</p>
     * <ul>
     * <li>{@link #CODE_LITERAL}</li>
     * <li>{@link #DOC_ROOT_LITERAL}</li>
     * <li>{@link #LINK_LITERAL}</li>
     * <li>{@link #INHERIT_DOC_LITERAL}</li>
     * <li>{@link #LINKPLAIN_LITERAL}</li>
     * <li>{@link #LITERAL_LITERAL}</li>
     * <li>{@link #VALUE_LITERAL}</li>
     * <li>or {@link #CUSTOM_NAME} if it is custom Javadoc inline tag.</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@literal {@link String} }</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *  JAVADOC_INLINE_TAG -> JAVADOC_INLINE_TAG
     *      |--JAVADOC_INLINE_TAG_START -> {
     *      |--LINK_LITERAL -> @link
     *      |--WS ->
     *      |--REFERENCE -> REFERENCE
     *          `--PACKAGE_CLASS -> String
     *      `--JAVADOC_INLINE_TAG_END -> }
     * }
     * </pre>
     */
    public static final int JAVADOC_INLINE_TAG = JavadocParser.RULE_javadocInlineTag
            + RULE_TYPES_OFFSET;

    /**
     * Parameter of the Javadoc tags listed below.
     * <ul>
     * <li>{@link #SEE_LITERAL @see}</li>
     * <li>{@link #LINK_LITERAL &#123;&#64;link&#125;}</li>
     * <li>{@link #LINKPLAIN_LITERAL &#123;&#64;linkplain&#125;}</li>
     * <li>{@link #VALUE_LITERAL &#123;&#64;value&#125;}</li>
     * </ul>
     */
    public static final int REFERENCE = JavadocParser.RULE_reference + RULE_TYPES_OFFSET;

    /**
     * Parameters part in {@link #REFERENCE}.
     * It is used to specify parameters for {@link #MEMBER method}.
     * Always contains {@link #LEFT_BRACE} as first child and {@link #RIGHT_BRACE} as last child.
     * Each parameter is represented by {@link #ARGUMENT} node.
     * Arguments in braces are separated by {@link #COMMA} (and optional {@link #WS}).
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @see #method(Processor, String)}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *      JAVADOC_TAG -> JAVADOC_TAG
     *          |--SEE_LITERAL -> @see
     *          |--WS ->
     *          |--REFERENCE -> REFERENCE
     *              |--HASH -> #
     *              |--MEMBER -> method
     *              `--PARAMETERS -> PARAMETERS
     *                  |--LEFT_BRACE -> (
     *                  |--ARGUMENT -> Processor
     *                  |--COMMA -> ,
     *                  |--WS ->
     *                  |--ARGUMENT -> String
     *                  `--RIGHT_BRACE -> )
     * }
     * </pre>
     */
    public static final int PARAMETERS = JavadocParser.RULE_parameters + RULE_TYPES_OFFSET;

    /**
     * Description node. It contains:
     * <ul>
     * <li>{@link #TEXT}</li>
     * <li>{@link #WS}</li>
     * <li>{@link #NEWLINE}</li>
     * <li>{@link #HTML_ELEMENT}</li>
     * </ul>
     *
     * <p>It is argument for many Javadoc tags and inline tags.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code @throws IOException if <b>connection</b> problems occur}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   --JAVADOC_TAG -> JAVADOC_TAG
     *      |--THROWS_LITERAL -> @throws
     *      |--WS ->
     *      |--CLASS_NAME -> IOException
     *      |--WS ->
     *      `--DESCRIPTION -> DESCRIPTION
     *          |--TEXT -> if
     *          |--HTML_ELEMENT -> HTML_ELEMENT
     *          |   `--HTML_TAG -> HTML_TAG
     *          |       |--HTML_ELEMENT_START -> HTML_ELEMENT_START
     *          |       |   |--START -> <
     *          |       |   |--HTML_TAG_NAME -> b
     *          |       |   `--END -> >
     *          |       |--TEXT -> connection
     *          |       `--HTML_ELEMENT_END -> HTML_ELEMENT_END
     *          |           |--START -> <
     *          |           |--SLASH -> /
     *          |           |--HTML_TAG_NAME -> b
     *          |           `--END -> >
     *          |--TEXT ->  problems occur
     * }
     * </pre>
     */
    public static final int DESCRIPTION = JavadocParser.RULE_description + RULE_TYPES_OFFSET;

    // ------------------------------------------------------------------------------------------ //
    // -------- HTML TAGS DEPENDING ON RULE TYPES OFFSET ---------------------------------------- //
    // ------------------------------------------------------------------------------------------ //

    /**
     * Parent node for all html tags.
     */
    public static final int HTML_ELEMENT = JavadocParser.RULE_htmlElement
            + RULE_TYPES_OFFSET;

    /**
     * Start html tag: {@code <XXXX>}.
     */
    public static final int HTML_ELEMENT_START = JavadocParser.RULE_htmlElementStart
            + RULE_TYPES_OFFSET;

    /**
     * End html tag: {@code <XXXX>}.
     */
    public static final int HTML_ELEMENT_END = JavadocParser.RULE_htmlElementEnd
            + RULE_TYPES_OFFSET;

    /**
     * Non-special HTML tag.
     */
    public static final int HTML_TAG = JavadocParser.RULE_htmlTag + RULE_TYPES_OFFSET;

    /**
     * Html tag attribute. Parent node for: {@code HTML_TAG_IDENT, EQUALS, ATTR_VALUE}.
     *
     * <p><b>Example</b></p>
     * <pre>{@code <p class="highlight">Sample text</p>}</pre>
     * <b>Tree</b>
     * <pre>
     * {@code
     *   HTML_ELEMENT -> HTML_ELEMENT
     *      `--PARAGRAPH -> PARAGRAPH
     *          |--P_TAG_START -> P_TAG_START
     *          |   |--START -> <
     *          |   |--P_HTML_TAG_NAME -> p
     *          |   |--WS ->
     *          |   |--ATTRIBUTE -> ATTRIBUTE
     *          |   |   |--HTML_TAG_NAME -> class
     *          |   |   |--EQUALS -> =
     *          |   |   `--ATTR_VALUE -> "highlight"
     *          |   `--END -> >
     *          |--TEXT -> Sample text
     *          `--P_TAG_END -> P_TAG_END
     *              |--START -> <
     *              |--SLASH -> /
     *              |--P_HTML_TAG_NAME -> p
     *              `--END -> >
     * }
     * </pre>
     */
    public static final int ATTRIBUTE = JavadocParser.RULE_attribute
            + RULE_TYPES_OFFSET;

    /////////////////////// HTML TAGS WITH OPTIONAL END TAG /////////////////////////////////////
    /**
     * Paragraph html tag.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code <p>Sample text</p>}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   `--JAVADOC -> JAVADOC
     *       |--NEWLINE -> \r\n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       |--HTML_ELEMENT -> HTML_ELEMENT
     *       |   `--PARAGRAPH -> PARAGRAPH
     *       |       |--P_TAG_START -> P_TAG_START
     *       |       |   |--START -> <
     *       |       |   |--P_HTML_TAG_NAME -> p
     *       |       |   `--END -> >
     *       |       |--TEXT -> Sample text
     *       |       `--P_TAG_END -> P_TAG_END
     *       |           |--START -> <
     *       |           |--SLASH -> /
     *       |           |--P_HTML_TAG_NAME -> p
     *       |           `--END -> >
     *       |--NEWLINE -> \r\n
     *       |--TEXT ->
     * }
     * </pre>
     */
    public static final int PARAGRAPH = JavadocParser.RULE_paragraph + RULE_TYPES_OFFSET;
    /** Start paragraph tag. */
    public static final int P_TAG_START = JavadocParser.RULE_pTagStart + RULE_TYPES_OFFSET;
    /** End paragraph tag. */
    public static final int P_TAG_END = JavadocParser.RULE_pTagEnd + RULE_TYPES_OFFSET;

    /**
     * List items html tag.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * <ul>
     *      <li>Item 1</li>
     * </ul>
     * }</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   --JAVADOC -> JAVADOC
     *     |--NEWLINE -> \n
     *     |--LEADING_ASTERISK ->  *
     *     |--TEXT ->
     *     |--HTML_ELEMENT -> HTML_ELEMENT
     *     |   `--HTML_TAG -> HTML_TAG
     *     |       |--HTML_ELEMENT_START -> HTML_ELEMENT_START
     *     |       |   |--START -> <
     *     |       |   |--HTML_TAG_NAME -> ul
     *     |       |   `--END -> >
     *     |       |--NEWLINE -> \n
     *     |       |--LEADING_ASTERISK ->  *
     *     |       |--TEXT ->
     *     |       |--HTML_ELEMENT -> HTML_ELEMENT
     *     |       |   `--LI -> LI
     *     |       |       |--LI_TAG_START -> LI_TAG_START
     *     |       |       |   |--START -> <
     *     |       |       |   |--LI_HTML_TAG_NAME -> li
     *     |       |       |   `--END -> >
     *     |       |       |--TEXT -> Item 1
     *     |       |       `--LI_TAG_END -> LI_TAG_END
     *     |       |           |--START -> <
     *     |       |           |--SLASH -> /
     *     |       |           |--LI_HTML_TAG_NAME -> li
     *     |       |           `--END -> >
     *     |       |--NEWLINE -> \n
     *     |       |--LEADING_ASTERISK ->  *
     *     |       |--TEXT ->
     *     |       `--HTML_ELEMENT_END -> HTML_ELEMENT_END
     *     |           |--START -> <
     *     |           |--SLASH -> /
     *     |           |--HTML_TAG_NAME -> ul
     *     |           `--END -> >
     *     |--NEWLINE -> \n
     *     |--TEXT ->
     * }
     * </pre>
     */
    public static final int LI = JavadocParser.RULE_li + RULE_TYPES_OFFSET;
    /** Start list item tag. */
    public static final int LI_TAG_START = JavadocParser.RULE_liTagStart + RULE_TYPES_OFFSET;
    /** End list item tag. */
    public static final int LI_TAG_END = JavadocParser.RULE_liTagEnd + RULE_TYPES_OFFSET;

    /**
     * Table row html tag.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code <tr></tr>}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   JAVADOC -> JAVADOC
     *        |--NEWLINE -> \r\n
     *        |--LEADING_ASTERISK ->  *
     *        |--TEXT ->
     *        |--HTML_ELEMENT -> HTML_ELEMENT
     *        |   `--TR -> TR
     *        |       |--TR_TAG_START -> TR_TAG_START
     *        |       |   |--START -> <
     *        |       |   |--TR_HTML_TAG_NAME -> tr
     *        |       |   `--END -> >
     *        |       `--TR_TAG_END -> TR_TAG_END
     *        |           |--START -> <
     *        |           |--SLASH -> /
     *        |           |--TR_HTML_TAG_NAME -> tr
     *        |           `--END -> >
     *        |--NEWLINE -> \r\n
     *        |--TEXT ->
     * }
     * </pre>
     */
    public static final int TR = JavadocParser.RULE_tr + RULE_TYPES_OFFSET;
    /** Start table row tag. */
    public static final int TR_TAG_START = JavadocParser.RULE_trTagStart + RULE_TYPES_OFFSET;
    /** End table row tag. */
    public static final int TR_TAG_END = JavadocParser.RULE_trTagEnd + RULE_TYPES_OFFSET;

    /**
     * Table cell HTML tag.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code <td>Cell Content</td>}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     * HTML_ELEMENT -> HTML_ELEMENT
     *    `--TD -> TD
     *        |--TD_TAG_START -> TD_TAG_START
     *        |   |--START -> <
     *        |   |--TD_HTML_TAG_NAME -> td
     *        |   `--END -> >
     *        |--TEXT -> Cell Content
     *        `--TD_TAG_END -> TD_TAG_END
     *            |--START -> <
     *            |--SLASH -> /
     *            |--TD_HTML_TAG_NAME -> td
     *            `--END -> >
     * }
     * </pre>
     */
    public static final int TD = JavadocParser.RULE_td + RULE_TYPES_OFFSET;
    /** Start table cell tag. */
    public static final int TD_TAG_START = JavadocParser.RULE_tdTagStart + RULE_TYPES_OFFSET;
    /** End table cell tag. */
    public static final int TD_TAG_END = JavadocParser.RULE_tdTagEnd + RULE_TYPES_OFFSET;

    /** Table header cell html tag: {@code <th></th>}. */
    public static final int TH = JavadocParser.RULE_th + RULE_TYPES_OFFSET;
    /** Start table header cell tag. */
    public static final int TH_TAG_START = JavadocParser.RULE_thTagStart + RULE_TYPES_OFFSET;
    /** End table header cell tag. */
    public static final int TH_TAG_END = JavadocParser.RULE_thTagEnd + RULE_TYPES_OFFSET;

    /** Body html tag. */
    public static final int BODY = JavadocParser.RULE_body + RULE_TYPES_OFFSET;

    /**
     * Start body tag.
     *
     * <p><b>Example:</b></p>
     * <pre>
     * &lt;body&gt;
     * This is a test
     * &lt;/body&gt;
     * </pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   JAVADOC -> JAVADOC
     *        |--TEXT -> /**
     *        |--NEWLINE -> \n
     *        |--LEADING_ASTERISK ->  *
     *        |--TEXT ->
     *        |--HTML_ELEMENT -> HTML_ELEMENT
     *        |    `--BODY -> BODY
     *        |         |--BODY_TAG_START -> BODY_TAG_START
     *        |         |    |--START -> <
     *        |         |    |--BODY_HTML_TAG_NAME -> body
     *        |         |    `--END -> >
     *        |         |--NEWLINE -> \n
     *        |         |--LEADING_ASTERISK ->  *
     *        |         |--TEXT ->  This is inside the body tag.
     *        |         |--NEWLINE -> \n
     *        |         |--LEADING_ASTERISK ->  *
     *        |         |--TEXT ->
     *        |         `--BODY_TAG_END -> BODY_TAG_END
     *        |             |--START -> <
     *        |             |--SLASH -> /
     *        |             |--BODY_HTML_TAG_NAME -> body
     *        |             `--END -> >
     *        |--NEWLINE -> \n
     *        |--LEADING_ASTERISK ->  *
     *        |--TEXT -> /
     *        |--NEWLINE -> \n
     *        |--TEXT -> public class Test {
     *        |--NEWLINE -> \n
     *        |--TEXT -> }
     *        |--NEWLINE -> \n
     * }
     * </pre>
     */
    public static final int BODY_TAG_START = JavadocParser.RULE_bodyTagStart + RULE_TYPES_OFFSET;

    /**
     * End body tag.
     *
     * <p><b>Example:</b></p>
     * <pre>
     *  &lt;body&gt;
     *     This is a test
     * &lt;/body&gt;
     * </pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   JAVADOC -> JAVADOC
     *        |--TEXT -> /**
     *        |--NEWLINE -> \n
     *        |--LEADING_ASTERISK ->  *
     *        |--TEXT ->
     *        |--HTML_ELEMENT -> HTML_ELEMENT
     *        |    `--BODY -> BODY
     *        |         |--BODY_TAG_START -> BODY_TAG_START
     *        |         |    |--START -> <
     *        |         |    |--BODY_HTML_TAG_NAME -> body
     *        |         |    `--END -> >
     *        |         |--NEWLINE -> \n
     *        |         |--LEADING_ASTERISK ->  *
     *        |         |--TEXT ->  This is inside the body tag.
     *        |         |--NEWLINE -> \n
     *        |         |--LEADING_ASTERISK ->  *
     *        |         |--TEXT ->
     *        |         `--BODY_TAG_END -> BODY_TAG_END
     *        |             |--START -> <
     *        |             |--SLASH -> /
     *        |             |--BODY_HTML_TAG_NAME -> body
     *        |             `--END -> >
     *        |--NEWLINE -> \n
     *        |--LEADING_ASTERISK ->  *
     *        |--TEXT -> /
     *        |--NEWLINE -> \n
     *        |--TEXT -> public class Test {
     *        |--NEWLINE -> \n
     *        |--TEXT -> }
     *        |--NEWLINE -> \n
     * }
     * </pre>
     */
    public static final int BODY_TAG_END = JavadocParser.RULE_bodyTagEnd + RULE_TYPES_OFFSET;

    /**
     * Colgroup html tag.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * <colgroup>
     *     <col />
     *     <col span="2" class="batman" />
     * </colgroup>
     * }</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   --JAVADOC -> JAVADOC
     *       |--NEWLINE -> \n
     *       |--LEADING_ASTERISK ->  *
     *       |--TEXT ->
     *       |--HTML_ELEMENT -> HTML_ELEMENT
     *       |   `--COLGROUP -> COLGROUP
     *       |       |--COLGROUP_TAG_START -> COLGROUP_TAG_START
     *       |       |   |--START -> <
     *       |       |   |--COLGROUP_HTML_TAG_NAME -> colgroup
     *       |       |   |   `--END -> >
     *       |       |--NEWLINE -> \n
     *       |       |--LEADING_ASTERISK ->  *
     *       |       |--TEXT ->
     *       |       |--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *       |       |   `--COL_TAG -> COL_TAG
     *       |       |       |--START -> <
     *       |       |       |--COL_HTML_TAG_NAME -> col
     *       |       |       |--WS ->
     *       |       |       `--SLASH_END -> />
     *       |       |--NEWLINE -> \n
     *       |       |--LEADING_ASTERISK ->  *
     *       |       |--TEXT ->
     *       |       |--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *       |       |   `--COL_TAG -> COL_TAG
     *       |       |       |--START -> <
     *       |       |       |--COL_HTML_TAG_NAME -> col
     *       |       |       |--WS ->
     *       |       |       |--ATTRIBUTE -> ATTRIBUTE
     *       |       |       |   |--HTML_TAG_NAME -> span
     *       |       |       |   |--EQUALS -> =
     *       |       |       |   `--ATTR_VALUE -> "2"
     *       |       |       |--WS ->
     *       |       |       |--ATTRIBUTE -> ATTRIBUTE
     *       |       |       |   |--HTML_TAG_NAME -> class
     *       |       |       |   |--EQUALS -> =
     *       |       |       |   `--ATTR_VALUE -> "batman"
     *       |       |       |--WS ->
     *       |       |       `--SLASH_END -> />
     *       |       |--NEWLINE -> \n
     *       |       |--LEADING_ASTERISK ->  *
     *       |       |--TEXT ->
     *       |       `--COLGROUP_TAG_END -> COLGROUP_TAG_END
     *       |       |       |--START -> <
     *       |       |       |--SLASH -> /
     *       |       |       |--COLGROUP_HTML_TAG_NAME -> colgroup
     *       |       |       `--END -> >
     *       |--NEWLINE -> \n
     *       |--TEXT ->
     * }
     * </pre>
     */
    public static final int COLGROUP = JavadocParser.RULE_colgroup + RULE_TYPES_OFFSET;
    /** Start colgroup tag. */
    public static final int COLGROUP_TAG_START = JavadocParser.RULE_colgroupTagStart
            + RULE_TYPES_OFFSET;
    /** End colgroup tag. */
    public static final int COLGROUP_TAG_END = JavadocParser.RULE_colgroupTagEnd
            + RULE_TYPES_OFFSET;

    /**
     * DD html tag.
     *
     * <p><b>Example AST:</b></p>
     * <pre>{@code <dd>Description content</dd>}</pre>
     * <pre>
     * {@code
     *   --HTML_ELEMENT -> HTML_ELEMENT
     *      `--DD -> DD
     *          |--DD_TAG_START -> DD_TAG_START
     *          |   |--START -> <
     *          |   |--DD_HTML_TAG_NAME -> dd
     *          |   `--END -> >
     *          |--TEXT -> "Description content"
     *          `--DD_TAG_END -> DD_TAG_END
     *              |--START -> <
     *              |--SLASH -> /
     *              |--DD_HTML_TAG_NAME -> dd
     *              `--END -> >
     * }
     * </pre>
     */
    public static final int DD = JavadocParser.RULE_dd + RULE_TYPES_OFFSET;
    /** Start description of a term tag. */
    public static final int DD_TAG_START = JavadocParser.RULE_ddTagStart + RULE_TYPES_OFFSET;
    /** End description of a term tag. */
    public static final int DD_TAG_END = JavadocParser.RULE_ddTagEnd + RULE_TYPES_OFFSET;

    /**
     * DT html tag.
     *
     * <p><b>Example AST:</b></p>
     * <pre>{@code <dd>Description Term</dd>}</pre>
     * <pre>
     * {@code
     *   --HTML_ELEMENT -> HTML_ELEMENT
     *      `--DT -> DT
     *          |--DT_TAG_START -> DT_TAG_START
     *          |   |--START -> <
     *          |   |--DT_HTML_TAG_NAME -> dt
     *          |   `--END -> >
     *          |--TEXT -> "Description term"
     *          `--DT_TAG_END -> DT_TAG_END
     *              |--START -> <
     *              |--SLASH -> /
     *              |--DT_HTML_TAG_NAME -> dt
     *              `--END -> >
     * }
     * </pre>
     */
    public static final int DT = JavadocParser.RULE_dt + RULE_TYPES_OFFSET;
    /** Start description term tag. */
    public static final int DT_TAG_START = JavadocParser.RULE_dtTagStart + RULE_TYPES_OFFSET;
    /** End description term tag. */
    public static final int DT_TAG_END = JavadocParser.RULE_dtTagEnd + RULE_TYPES_OFFSET;

    /** Head html tag. */
    public static final int HEAD = JavadocParser.RULE_head + RULE_TYPES_OFFSET;
    /** Start head tag. */
    public static final int HEAD_TAG_START = JavadocParser.RULE_headTagStart + RULE_TYPES_OFFSET;
    /** End head tag. */
    public static final int HEAD_TAG_END = JavadocParser.RULE_headTagEnd + RULE_TYPES_OFFSET;

    /** Html html tag. */
    public static final int HTML = JavadocParser.RULE_html + RULE_TYPES_OFFSET;
    /** Start html tag. */
    public static final int HTML_TAG_START = JavadocParser.RULE_htmlTagStart + RULE_TYPES_OFFSET;
    /** End html tag. */
    public static final int HTML_TAG_END = JavadocParser.RULE_htmlTagEnd + RULE_TYPES_OFFSET;

    /** Option html tag. */
    public static final int OPTION = JavadocParser.RULE_option + RULE_TYPES_OFFSET;
    /** Start option tag. */
    public static final int OPTION_TAG_START =
            JavadocParser.RULE_optionTagStart + RULE_TYPES_OFFSET;
    /** End option tag. */
    public static final int OPTION_TAG_END = JavadocParser.RULE_optionTagEnd
            + RULE_TYPES_OFFSET;

    /**
     * Table body html tag.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code <table><tbody></tbody></table>}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *    JAVADOC -> JAVADOC
     *      |--NEWLINE -> \r\n
     *      |--LEADING_ASTERISK ->  *
     *      |--TEXT ->
     *      |--HTML_ELEMENT -> HTML_ELEMENT
     *      |   `--HTML_TAG -> HTML_TAG
     *      |       |--HTML_ELEMENT_START -> HTML_ELEMENT_START
     *      |       |   |--START -> <
     *      |       |   |--HTML_TAG_NAME -> table
     *      |       |   `--END -> >
     *      |       |--HTML_ELEMENT -> HTML_ELEMENT
     *      |       |   `--TBODY -> TBODY
     *      |       |       |--TBODY_TAG_START -> TBODY_TAG_START
     *      |       |       |   |--START -> <
     *      |       |       |   |--TBODY_HTML_TAG_NAME -> tbody
     *      |       |       |   `--END -> >
     *      |       |       `--TBODY_TAG_END -> TBODY_TAG_END
     *      |       |           |--START -> <
     *      |       |           |--SLASH -> /
     *      |       |           |--TBODY_HTML_TAG_NAME -> tbody
     *      |       |           `--END -> >
     *      |       `--HTML_ELEMENT_END -> HTML_ELEMENT_END
     *      |           |--START -> <
     *      |           |--SLASH -> /
     *      |           |--HTML_TAG_NAME -> table
     *      |           `--END -> >
     *      |--NEWLINE -> \r\n
     * }
     * </pre>
     */
    public static final int TBODY = JavadocParser.RULE_tbody + RULE_TYPES_OFFSET;
    /** Start table body tag. */
    public static final int TBODY_TAG_START = JavadocParser.RULE_tbodyTagStart + RULE_TYPES_OFFSET;
    /** End table body tag. */
    public static final int TBODY_TAG_END = JavadocParser.RULE_tbodyTagEnd + RULE_TYPES_OFFSET;

    /** Table foot html tag. */
    public static final int TFOOT = JavadocParser.RULE_tfoot + RULE_TYPES_OFFSET;
    /** Start table foot tag. */
    public static final int TFOOT_TAG_START = JavadocParser.RULE_tfootTagStart + RULE_TYPES_OFFSET;
    /** End table foot tag. */
    public static final int TFOOT_TAG_END = JavadocParser.RULE_tfootTagEnd + RULE_TYPES_OFFSET;

    /** Table head html tag. */
    public static final int THEAD = JavadocParser.RULE_thead + RULE_TYPES_OFFSET;
    /** Start table head tag. */
    public static final int THEAD_TAG_START = JavadocParser.RULE_theadTagStart + RULE_TYPES_OFFSET;
    /** End table head tag. */
    public static final int THEAD_TAG_END = JavadocParser.RULE_theadTagEnd + RULE_TYPES_OFFSET;

    /** `optgroup` html tag. */
    public static final int OPTGROUP = JavadocParser.RULE_optgroup + RULE_TYPES_OFFSET;
    /** `optgroup` tag start. */
    public static final int OPTGROUP_TAG_START =
            JavadocParser.RULE_optgroupTagStart + RULE_TYPES_OFFSET;
    /** `optgroup` tag end. */
    public static final int OPTGROUP_TAG_END =
            JavadocParser.RULE_optgroupTagEnd + RULE_TYPES_OFFSET;

    /** `rb` html tag. */
    public static final int RB = JavadocParser.RULE_rb + RULE_TYPES_OFFSET;
    /** `rb` tag start. */
    public static final int RB_TAG_START =
            JavadocParser.RULE_rbTagStart + RULE_TYPES_OFFSET;
    /** `rb` tag end. */
    public static final int RB_TAG_END =
            JavadocParser.RULE_rbTagEnd + RULE_TYPES_OFFSET;

    /** `rt` html tag. */
    public static final int RT = JavadocParser.RULE_rt + RULE_TYPES_OFFSET;
    /** `rt` tag start. */
    public static final int RT_TAG_START =
            JavadocParser.RULE_rtTagStart + RULE_TYPES_OFFSET;
    /** `rt` tag end. */
    public static final int RT_TAG_END =
            JavadocParser.RULE_rtTagEnd + RULE_TYPES_OFFSET;

    /** `rtc` html tag. */
    public static final int RTC = JavadocParser.RULE_rtc + RULE_TYPES_OFFSET;
    /** `rtc` tag start. */
    public static final int RTC_TAG_START =
            JavadocParser.RULE_rtcTagStart + RULE_TYPES_OFFSET;
    /** `rtc` tag end. */
    public static final int RTC_TAG_END =
            JavadocParser.RULE_rtcTagEnd + RULE_TYPES_OFFSET;

    /** `rp` html tag. */
    public static final int RP = JavadocParser.RULE_rp + RULE_TYPES_OFFSET;
    /** `rp` tag start. */
    public static final int RP_TAG_START =
            JavadocParser.RULE_rpTagStart + RULE_TYPES_OFFSET;
    /** `rp` tag end. */
    public static final int RP_TAG_END =
            JavadocParser.RULE_rpTagEnd + RULE_TYPES_OFFSET;

    /////////////////////// SINGLETON HTML TAGS  //////////////////////////////////////////////////
    /**
     * Parent node for all singleton html tags.
     */
    public static final int SINGLETON_ELEMENT = JavadocParser.RULE_singletonElement
            + RULE_TYPES_OFFSET;

    /**
     * Represents an empty (self-closing) HTML tag in Javadoc comments,
     * such as {@code <justsometag />}.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code <justsometag />}</pre>
     *
     * <p><b>AST Tree:</b></p>
     * <pre>{@code
     * --HTML_ELEMENT -> HTML_ELEMENT
     *   `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *     `--EMPTY_TAG -> EMPTY_TAG
     *       |--START -> <
     *       |--HTML_TAG_NAME -> justsometag
     *       |--WS
     *       `--SLASH_END -> >
     * }</pre>
     */
    public static final int EMPTY_TAG =
        JavadocParser.RULE_emptyTag + RULE_TYPES_OFFSET;

    /**
     * Area html tag.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code < area shape="rect" >}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   JAVADOC -> JAVADOC
     *        |--NEWLINE -> \n
     *        |--LEADING_ASTERISK ->  *
     *        |--TEXT ->
     *        |--HTML_ELEMENT -> HTML_ELEMENT
     *        |   `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *        |       `--AREA_TAG -> AREA_TAG
     *        |           |--START -> <
     *        |           |--AREA_HTML_TAG_NAME -> area
     *        |           |--WS ->
     *        |           |--ATTRIBUTE -> ATTRIBUTE
     *        |           |   |--HTML_TAG_NAME -> shape
     *        |           |   |--EQUALS -> =
     *        |           |   `--ATTR_VALUE -> "rect"
     *        |           `--END -> >
     *        |--TEXT ->
     *        |--NEWLINE -> \n
     *        |--TEXT ->
     * }
     * </pre>
     */
    public static final int AREA_TAG = JavadocParser.RULE_areaTag + RULE_TYPES_OFFSET;

    /** Base html tag. */
    public static final int BASE_TAG = JavadocParser.RULE_baseTag + RULE_TYPES_OFFSET;

    /** Basefont html tag. */
    public static final int BASEFONT_TAG = JavadocParser.RULE_basefontTag + RULE_TYPES_OFFSET;

    /**
     * Br html tag.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code <br> line breaks<br/>}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     * JAVADOC -> JAVADOC
     * |--NEWLINE -> \r\n
     * |--LEADING_ASTERISK ->  *
     * |--TEXT ->
     * |--HTML_ELEMENT -> HTML_ELEMENT
     * |   `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     * |       `--BR_TAG -> BR_TAG
     * |           |--START -> <
     * |           |--BR_HTML_TAG_NAME -> br
     * |           `--END -> >
     * |--TEXT ->  line breaks
     * |--HTML_ELEMENT -> HTML_ELEMENT
     * |   `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     * |       `--BR_TAG -> BR_TAG
     * |           |--START -> <
     * |           |--BR_HTML_TAG_NAME -> br
     * |           `--SLASH_END -> />
     * }
     * </pre>
     */
    public static final int BR_TAG = JavadocParser.RULE_brTag + RULE_TYPES_OFFSET;

    /** Col html tag. */
    public static final int COL_TAG = JavadocParser.RULE_colTag + RULE_TYPES_OFFSET;

    /**
     * Frame html tag.
     *
     * <p><b>Example:</b></p>
     * <pre>
     * &lt;frameset cols="50%,50%"&gt;
     * &lt;frame src="page1.html"&gt;
     * &lt;/frameset&gt;
     * </pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     * HTML_ELEMENT -> HTML_ELEMENT
     *  `--HTML_TAG -> HTML_TAG
     *     |--HTML_ELEMENT_START -> HTML_ELEMENT_START
     *     |   |--START -> <
     *     |   |--HTML_TAG_NAME -> frameset
     *     |   |--WS ->
     *     |   |--ATTRIBUTE -> ATTRIBUTE
     *     |   |   |--HTML_TAG_NAME -> cols
     *     |   |   |--EQUALS -> =
     *     |   |   `--ATTR_VALUE -> "50%,50%"
     *     |   `--END -> >
     *     |--NEWLINE -> \r\n
     *     |--LEADING_ASTERISK ->  *
     *     |--TEXT ->
     *     |--HTML_ELEMENT -> HTML_ELEMENT
     *     |   `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *     |       `--FRAME_TAG -> FRAME_TAG
     *     |           |--START -> <
     *     |           |--FRAME_HTML_TAG_NAME -> frame
     *     |           |--WS ->
     *     |           |--ATTRIBUTE -> ATTRIBUTE
     *     |           |   |--HTML_TAG_NAME -> src
     *     |           |   |--EQUALS -> =
     *     |           |   `--ATTR_VALUE -> "page1.html"
     *     |           `--END -> >
     *     |--NEWLINE -> \r\n
     *     |--LEADING_ASTERISK ->  *
     *     |--TEXT ->
     *     `--HTML_ELEMENT_END -> HTML_ELEMENT_END
     *         |--START -> <
     *         |--SLASH -> /
     *         |--HTML_TAG_NAME -> frameset
     *         `--END -> >
     * }
     * </pre>
     */
    public static final int FRAME_TAG = JavadocParser.RULE_frameTag + RULE_TYPES_OFFSET;

    /**
     * Hr html tag.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code text above line<hr> test after line}</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   |--LEADING_ASTERISK ->  *
     *   |--TEXT ->  text above line
     *   |--HTML_ELEMENT -> HTML_ELEMENT
     *   |   `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *   |       `--HR_TAG -> HR_TAG
     *   |           |--START -> <
     *   |           |--HR_HTML_TAG_NAME -> hr
     *   |           `--END -> >
     *   |--TEXT ->  test after line
     *   |--NEWLINE -> \r\n
     *   |--TEXT ->
     * }
     * </pre>
     */
    public static final int HR_TAG = JavadocParser.RULE_hrTag + RULE_TYPES_OFFSET;

    /**
     * Img html tag.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code <img src="./image.png" alt="image description" width="200" height="100"> }</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   `--JAVADOC -> JAVADOC
     *        |--NEWLINE -> \r\n
     *        |--LEADING_ASTERISK ->  *
     *        |--TEXT ->
     *        |--HTML_ELEMENT -> HTML_ELEMENT
     *        |   `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *        |       `--IMG_TAG -> IMG_TAG
     *        |           |--START -> <
     *        |           |--IMG_HTML_TAG_NAME -> img
     *        |           |--WS ->
     *        |           |--ATTRIBUTE -> ATTRIBUTE
     *        |           |   |--HTML_TAG_NAME -> src
     *        |           |   |--EQUALS -> =
     *        |           |   `--ATTR_VALUE -> "./image.png"
     *        |           |--WS ->
     *        |           |--ATTRIBUTE -> ATTRIBUTE
     *        |           |   |--HTML_TAG_NAME -> alt
     *        |           |   |--EQUALS -> =
     *        |           |   `--ATTR_VALUE -> "image description"
     *        |           |--WS ->
     *        |           |--ATTRIBUTE -> ATTRIBUTE
     *        |           |   |--HTML_TAG_NAME -> width
     *        |           |   |--EQUALS -> =
     *        |           |   `--ATTR_VALUE -> "200"
     *        |           |--WS ->
     *        |           |--ATTRIBUTE -> ATTRIBUTE
     *        |           |   |--HTML_TAG_NAME -> height
     *        |           |   |--EQUALS -> =
     *        |           |   `--ATTR_VALUE -> "100"
     *        |           `--END -> >
     *        |--NEWLINE -> \r\n
     *        |--TEXT ->
     * }
     * </pre>
     */
    public static final int IMG_TAG = JavadocParser.RULE_imgTag + RULE_TYPES_OFFSET;

    /**
     * Input html tag.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code Type here: <input type="text" id="id" name="name"> }</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   JAVADOC -> JAVADOC
     *        |--NEWLINE -> \r\n
     *        |--LEADING_ASTERISK ->  *
     *        |--TEXT ->   Type here:
     *        |--HTML_ELEMENT -> HTML_ELEMENT
     *        |   `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *        |       `--INPUT_TAG -> INPUT_TAG
     *        |           |--START -> <
     *        |           |--INPUT_HTML_TAG_NAME -> input
     *        |           |--WS ->
     *        |           |--ATTRIBUTE -> ATTRIBUTE
     *        |           |   |--HTML_TAG_NAME -> type
     *        |           |   |--EQUALS -> =
     *        |           |   `--ATTR_VALUE -> "text"
     *        |           |--WS ->
     *        |           |--ATTRIBUTE -> ATTRIBUTE
     *        |           |   |--HTML_TAG_NAME -> id
     *        |           |   |--EQUALS -> =
     *        |           |   `--ATTR_VALUE -> "id"
     *        |           |--WS ->
     *        |           |--ATTRIBUTE -> ATTRIBUTE
     *        |           |   |--HTML_TAG_NAME -> name
     *        |           |   |--EQUALS -> =
     *        |           |   `--ATTR_VALUE -> "name"
     *        |           `--END -> >
     *        |--NEWLINE -> \r\n
     *        |--TEXT ->
     * }
     * </pre>
     */
    public static final int INPUT_TAG = JavadocParser.RULE_inputTag + RULE_TYPES_OFFSET;

    /**
     * Isindex tag name.
     *
     * <p><b>Example:</b></p>
     * <pre>
     * &lt;head&gt;
     *    &lt;isindex prompt="search"&gt;
     * &lt;/head&gt;
     * </pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   |--HTML_ELEMENT -> HTML_ELEMENT
     *   |   `--HEAD -> HEAD
     *   |       |--HEAD_TAG_START -> HEAD_TAG_START
     *   |       |   |--START -> <
     *   |       |   |--HEAD_HTML_TAG_NAME -> head
     *   |       |   `--END -> >
     *   |       |--NEWLINE -> \r\n
     *   |       |--LEADING_ASTERISK ->  *
     *   |       |--TEXT ->
     *   |       |--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *   |       |   `--ISINDEX_TAG -> ISINDEX_TAG
     *   |       |       |--START -> <
     *   |       |       |--ISINDEX_HTML_TAG_NAME -> isindex
     *   |       |       |--WS ->
     *   |       |       |--ATTRIBUTE -> ATTRIBUTE
     *   |       |       |   |--HTML_TAG_NAME -> prompt
     *   |       |       |   |--EQUALS -> =
     *   |       |       |   `--ATTR_VALUE -> "search"
     *   |       |       `--END -> >
     *   |       |--NEWLINE -> \r\n
     *   |       |--LEADING_ASTERISK ->  *
     *   |       |--TEXT ->
     *   |       `--HEAD_TAG_END -> HEAD_TAG_END
     *   |           |--START -> <
     *   |           |--SLASH -> /
     *   |           |--HEAD_HTML_TAG_NAME -> head
     *   |           `--END -> >
     *   |--NEWLINE -> \r\n
     *   |--TEXT ->
     * }
     * </pre>
     */
    public static final int ISINDEX_TAG = JavadocParser.RULE_isindexTag + RULE_TYPES_OFFSET;

    /**
     * Link html tag.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code <link rel="stylesheet" href="styles.css">}</pre>
     *
     * <b>Tree:</b>
     * <pre>
     * {@code
     * HTML_ELEMENT -> HTML_ELEMENT
     *     `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *      `--LINK_TAG -> LINK_TAG
     *          |--START -> <
     *          |--LINK_HTML_TAG_NAME -> link
     *          |--ATTRIBUTE -> ATTRIBUTE
     *          |   |--HTML_TAG_NAME -> rel
     *          |   |--EQUALS -> =
     *          |   `--ATTR_VALUE -> "stylesheet"
     *          |--ATTRIBUTE -> ATTRIBUTE
     *          |   |--HTML_TAG_NAME -> href
     *          |   |--EQUALS -> =
     *          |   `--ATTR_VALUE -> "styles.css"
     *          `--END -> >
     * }
     * </pre>
     */
    public static final int LINK_TAG = JavadocParser.RULE_linkTag + RULE_TYPES_OFFSET;

    /**
     * Meta html tag.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code <meta charset="UTF-8"> }</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *   `--JAVADOC -> JAVADOC
     *        |--NEWLINE -> \r\n
     *        |--LEADING_ASTERISK ->  *
     *        |--TEXT ->
     *        |--HTML_ELEMENT -> HTML_ELEMENT
     *        |   `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *        |       `--META_TAG -> META_TAG
     *        |           |--START -> <
     *        |           |--META_HTML_TAG_NAME -> meta
     *        |           |--WS ->
     *        |           |--ATTRIBUTE -> ATTRIBUTE
     *        |           |   |--HTML_TAG_NAME -> charset
     *        |           |   |--EQUALS -> =
     *        |           |   `--ATTR_VALUE -> "UTF-8"
     *        |           `--END -> >
     *        |--NEWLINE -> \r\n
     *        |--TEXT ->
     * }
     * </pre>
     */
    public static final int META_TAG = JavadocParser.RULE_metaTag + RULE_TYPES_OFFSET;

    /** Param html tag. */
    public static final int PARAM_TAG = JavadocParser.RULE_paramTag + RULE_TYPES_OFFSET;

    /**
     * HTML void element.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * <embed src="URL" type="MIME_type">
     * }</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *    |--HTML_ELEMENT -> HTML_ELEMENT
     *    |   `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *    |       `--EMBED_TAG -> EMBED_TAG
     *    |           |--START -> <
     *    |           |--EMBED_HTML_TAG_NAME -> embed
     *    |           |--WS ->
     *    |           |--ATTRIBUTE -> ATTRIBUTE
     *    |           |   |--HTML_TAG_NAME -> src
     *    |           |   |--EQUALS -> =
     *    |           |   `--ATTR_VALUE -> "URL"
     *    |           |--WS ->
     *    |           |--ATTRIBUTE -> ATTRIBUTE
     *    |           |   |--HTML_TAG_NAME -> type
     *    |           |   |--EQUALS -> =
     *    |           |   `--ATTR_VALUE -> "MIME_type"
     *    |           `--END -> >
     *    |--NEWLINE -> \r\n
     *    |--TEXT ->
     * }
     * </pre>
     *
     * @see #EMBED_TAG
     * @see <a href="https://www.w3.org/TR/html51/semantics-embedded-content.html#elementdef-embed">
     *     W3 docs</a>
     */
    public static final int EMBED_TAG = JavadocParser.RULE_embedTag + RULE_TYPES_OFFSET;

    /**
     * HTML void element {@code <keygen>}.
     *
     * @see #SINGLETON_ELEMENT
     * @see <a href="https://www.w3.org/TR/html51/sec-forms.html#elementdef-keygen">
     *     W3 docs</a>
     */
    public static final int KEYGEN_TAG = JavadocParser.RULE_keygenTag + RULE_TYPES_OFFSET;

    /**
     * HTML void element {@code <source>}.
     *
     * @see #SINGLETON_ELEMENT
     * @see <a href=
     *     "https://www.w3.org/TR/html51/semantics-embedded-content.html#elementdef-media-source">
     *     W3 docs</a>
     */
    public static final int SOURCE_TAG = JavadocParser.RULE_sourceTag + RULE_TYPES_OFFSET;

    /**
     * HTML void element {@code <track>}.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * <track kind="subtitles" src="subtitles_en.file" />
     * }</pre>
     * <b>Tree:</b>
     * <pre>
     * {@code
     *    |--HTML_ELEMENT -> HTML_ELEMENT
     *    |   `--SINGLETON_ELEMENT -> SINGLETON_ELEMENT
     *    |       `--TRACK_TAG -> TRACK_TAG
     *    |           |--START -> <
     *    |           |--TRACK_HTML_TAG_NAME -> track
     *    |           |--WS ->
     *    |           |--ATTRIBUTE -> ATTRIBUTE
     *    |           |   |--HTML_TAG_NAME -> kind
     *    |           |   |--EQUALS -> =
     *    |           |   `--ATTR_VALUE -> "subtitles"
     *    |           |--WS ->
     *    |           |--ATTRIBUTE -> ATTRIBUTE
     *    |           |   |--HTML_TAG_NAME -> src
     *    |           |   |--EQUALS -> =
     *    |           |   `--ATTR_VALUE -> "subtitles_en.file"
     *    |           `--END -> />
     *    |--NEWLINE -> \r\n
     *    |--TEXT ->
     * }
     * </pre>
     *
     * @see #TRACK_TAG
     * @see <a href="https://www.w3.org/TR/html51/semantics-embedded-content.html#elementdef-track">
     *     W3 docs</a>
     */
    public static final int TRACK_TAG = JavadocParser.RULE_trackTag + RULE_TYPES_OFFSET;

    /**
     * HTML void element {@code <wbr>}.
     *
     * @see #SINGLETON_ELEMENT
     * @see <a href="https://www.w3.org/TR/html51/textlevel-semantics.html#elementdef-wbr">
     *     W3 docs</a>
     */
    public static final int WBR_TAG = JavadocParser.RULE_wbrTag + RULE_TYPES_OFFSET;

    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Html comment: {@code <!-- --> }.
     */
    public static final int HTML_COMMENT = JavadocParser.RULE_htmlComment
            + RULE_TYPES_OFFSET;
    /**
     * CHAR and WS sequence.
     */
    public static final int TEXT = JavadocParser.RULE_text + RULE_TYPES_OFFSET;

    /** Empty private constructor of the current class. */
    private JavadocTokenTypes() {
    }

}

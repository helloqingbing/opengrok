/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * See LICENSE.txt included in this distribution for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright (c) 2012, 2016, Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright (c) 2017, Chris Fraire <cfraire@me.com>.
 */

package org.opensolaris.opengrok.analysis.document;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;

import org.opensolaris.opengrok.analysis.CtagsReader;
import org.opensolaris.opengrok.analysis.Definitions;
import org.opensolaris.opengrok.analysis.FileAnalyzer;
import org.opensolaris.opengrok.analysis.WriteXrefArgs;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.opensolaris.opengrok.util.CustomAssertions.assertLinesEqual;
import static org.opensolaris.opengrok.util.StreamUtils.copyStream;

/**
 * Tests the {@link TroffXref} class.
 */
public class TroffXrefTest {

    @Test
    public void sampleTest() throws IOException {
        writeAndCompare("org/opensolaris/opengrok/analysis/document/sync.1m",
            "org/opensolaris/opengrok/analysis/document/sync_xref.html",
            null);
    }

    private void writeAndCompare(String sourceResource, String resultResource,
        Definitions defs)
            throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        InputStream res = getClass().getClassLoader().getResourceAsStream(
            sourceResource);
        assertNotNull(sourceResource + " should get-as-stream", res);
        writeTroffXref(new PrintStream(baos), res, defs);
        res.close();

        InputStream exp = getClass().getClassLoader().getResourceAsStream(
            resultResource);
        assertNotNull(resultResource + " should get-as-stream", exp);
        byte[] expbytes = copyStream(exp);
        exp.close();
        baos.close();

        String ostr = new String(baos.toByteArray(), "UTF-8");
        String gotten[] = ostr.split("\n");

        String estr = new String(expbytes, "UTF-8");
        String expected[] = estr.split("\n");

        assertLinesEqual("Troff xref", expected, gotten);
    }

    private void writeTroffXref(PrintStream oss, InputStream iss,
        Definitions defs)
            throws IOException {

        oss.print(getHtmlBegin());

        Writer sw = new StringWriter();
        TroffAnalyzerFactory fac = new TroffAnalyzerFactory();
        FileAnalyzer analyzer = fac.getAnalyzer();
        analyzer.setScopesEnabled(true);
        analyzer.setFoldingEnabled(true);
        WriteXrefArgs wargs = new WriteXrefArgs(
            new InputStreamReader(iss, "UTF-8"), sw);
        wargs.setDefs(defs);
        analyzer.writeXref(wargs);
        oss.print(sw.toString());

        oss.print(getHtmlEnd());
    }

    private Definitions getTagsDefinitions() throws IOException {
        InputStream res = getClass().getClassLoader().getResourceAsStream(
            "org/opensolaris/opengrok/analysis/document/sampletags");
        assertNotNull("though sampletags should stream,", res);

        BufferedReader in = new BufferedReader(new InputStreamReader(
            res, "UTF-8"));

        CtagsReader rdr = new CtagsReader();
        String line;
        while ((line = in.readLine()) != null) {
            rdr.readLine(line);
        }
        return rdr.getDefinitions();
    }

    private static String getHtmlBegin() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n" +
            "    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\"" +
            " xml:lang=\"en\" lang=\"en\"\n" +
            "      class=\"xref\">\n" +
            "<head>\n" +
            "<title>sampleFile - OpenGrok cross reference" +
            " for /sampleFile</title></head><body><pre>\n";
    }

    private static String getHtmlEnd() {
        return "</pre></body>\n" +
            "</html>\n";
    }
}

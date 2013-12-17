<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/">
        <html>
            <head>
                <title>Simian Threshold Violations</title>
                <style type="text/css">
                    body {font-family:verdana, sans-serif; color:#FFFFEF; font-size:75%; margin:0px;}
                    th {font-family:verdana, sans-serif; font-size:75%; color:#FFFFFF; background:#006666;}
                    td {font-family:verdana, sans-serif; font-size:75%; color:#FF0000; background:#99CCCC;}
                    h1 {font-family:verdana, sans-serif; font-size:100%; color:#000000;}
                </style>
            </head>
            <body>
                <h1>Simian Threshold Violations (Threshold: <xsl:value-of select="simian/check/@threshold"/>)</h1>
                <table border="1" cellspacing="0" cellpadding="2">
                    <tr>
                        <th width="5%">Number of Lines Duplicated</th>
                        <th width="85%">Files with Duplication</th>
                        <th width="5%">Start Line Number</th>
                        <th width="5%">End Line Number</th>
                    </tr>
                    <xsl:for-each select="/simian/check/set">
                        <tr>
                            <td>
                                <xsl:value-of select="@lineCount"/>
                            </td>
                            <td>
                                <xsl:for-each select="block">
                                    ...<xsl:value-of select="substring-after(@sourceFile, 'src')"/>
                                    <br/>
                                </xsl:for-each>
                            </td>
                            <td>
                                <xsl:for-each select="block">
                                    <xsl:value-of select="@startLineNumber"/>
                                    <br/>
                                </xsl:for-each>
                            </td>
                            <td>
                                <xsl:for-each select="block">
                                    <xsl:value-of select="@endLineNumber"/>
                                    <br/>
                                </xsl:for-each>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
                <h1>Analysis Summary</h1>
                <table border="1" cellspacing="0" cellpadding="2">
                    <xsl:for-each select="simian/check/summary/@*">
                        <tr>
                            <th><xsl:value-of select="name()"/></th>
                            <td><xsl:value-of select="."/></td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>

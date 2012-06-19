<?xml-stylesheet type="text/xsl" href="rssfeed.xsl"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:dc="http://purl.org/dc/elements/1.1/" version="1.0">
<!-- rssfeed.xsl from http://ajamyajax.com -->
<!-- note: supports most rss feed formats but not all -->

<xsl:output method="html"/>
    <xsl:template match="rss/channel">
      <div class="rssdoctitle">
        <style>
            
            body {
                font-family: Arial
            }
        </style>

        <h1><xsl:value-of select="title"/></h1>
        <i style="position: relative; top: -15px">
            <xsl:value-of select="description" disable-output-escaping="yes"/>
        </i>
        
        <p />
        
        <div style="background-color: #ffd; border: 2px dashed black; padding: 5px; border-radius: 15px">
            <b>What is this?</b><br />
            This is the RGraph RSS/XML feed that you can use to keep up-to-date with RGraph news. You can use a feed reader such as
            <a href="http://www.google.com/reader/view/#stream/feed%2Fhttp%3A%2F%2Fwww.rgraph.net%2Fnews.xml" target="_blank">Google Reader</a> to subscribe to it. You'll then be notified
            when theres new news for you to read.
            
            <br /><br />
            
            <b>Why does it not look like other RSS/XML files I've seen?</b><br />
            This feed is formatted using an XML stylesheet (XLST). You can copy the address and paste it directly into your preferred
            feed reader (eg <a href="http://www.google.com/reader/view/#stream/feed%2Fhttp%3A%2F%2Fdev.rgraph.net%2Fnews.xml" target="_blank">Google Reader</a>).
        </div>
        
        <p />
        
        <xsl:if test="pubDate">
          <xsl:value-of select="pubDate"/><br/>
        </xsl:if>
        <xsl:if test="dc:date">
          <xsl:value-of select="dc:date"/> <br/>
        </xsl:if>
        <xsl:if test="updated">
          Updated: <xsl:value-of select="updated"/> <br/>
        </xsl:if>
      </div>
    
      <div class="rssitems">
      <xsl:for-each select="item">
        <div style="background-color: #eee; padding: 5px; border: 1px solid gray; margin-bottom: 25px; border-radius: 5px">
        <a href="{link}"><b><xsl:value-of select="title"/></b></a> <br/>
        <xsl:value-of select="description" disable-output-escaping="yes"/> <br/>
        <xsl:if test="dc:creator">
          By: <xsl:value-of select="dc:creator"/> <br/>
        </xsl:if>
        <xsl:if test="pubDate">
          <div class="rsspubdate"><i><xsl:value-of select="pubDate"/></i></div>
        </xsl:if>
        <xsl:if test="dc:date">
          <div class="rsspubdate">Posted: <xsl:value-of select="dc:date"/> </div> <br/>
        </xsl:if>
        <xsl:if test="updated">
          Updated: <xsl:value-of select="updated"/>
        </xsl:if>
        </div>
      </xsl:for-each>
      </div>
    </xsl:template>
</xsl:stylesheet>

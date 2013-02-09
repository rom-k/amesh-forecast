#!/usr/bin/python
# -*- coding: utf-8 -*-
#
# Copyleft rom.cpp@gmail.com

import webapp2
import string
import logging
#import json
import re
from google.appengine.api import urlfetch
#from google.appengine.api import images
from google.appengine.ext import db


class Gifdat(db.Model):
    filename = db.StringProperty(required=True)
    content = db.BlobProperty()


class Fetchdata(webapp2.RequestHandler):
    def get(self):
        logging.info("fetch : called : " + self.request.url)

        # Connect to tokyo-amesh site to get date&time list.
        amesh_url1 = "http://tokyo-ame.jwa.or.jp/scripts/mesh_index.js"
        result = urlfetch.fetch(amesh_url1)

        # Raise exception when access error occured.
        if result.status_code != 200:
            raise

        res = result.content.rstrip()
        logging.info("fetch : result.content : " + res)

        # Use string.translate to remove chars except for "1234567890,".
        allchars = string.maketrans('', '')
        delchars = allchars.translate(allchars, string.digits + ',')
        amesh_datetimes = res.translate(allchars, delchars).split(",")

        # Get date&time list from datastore.
        gifdats = db.GqlQuery("SELECT * FROM Gifdat")
        db_datetimes = [x.filename for x in gifdats]
        logging.info("fetch : db_datetimes : " + ','.join(db_datetimes))

        # Add new gif to datastore if not added yet.
        for datetime in amesh_datetimes:
            if datetime in db_datetimes:
                continue

            logging.info("fetch : add : " + datetime)
            db_datetimes.append(datetime)

            # Get gif image from tokyo amesh site.
            amesh_url2 = ("http://tokyo-ame.jwa.or.jp/mesh/100/" +
                          datetime + ".gif")
            result = urlfetch.fetch(amesh_url2)
            if result.status_code != 200:
                raise

            # Create new Gifdat instance and add to data store.
            gifdat = Gifdat(filename=datetime)
            gifdat.content = db.Blob(result.content)
            gifdat.put()

        # Remove old image from data store.
        # (Keep 1 week = 60/5*24*7 data.)
        db_datetimes.sort()
        keep = db_datetimes[:2016]
        gifdats = db.GqlQuery("SELECT * FROM Gifdat")
        for gifdat in gifdats:
            datetime = gifdat.filename
            if datetime not in keep:
                logging.info("fetch : remove : " + datetime)
                gifdat.delete()


class DateTimes(webapp2.RequestHandler):
    def get(self):
        logging.info("datetimes : called : " + self.request.url)

        # Output date&time list for gifdat.
        gifdats = db.GqlQuery("SELECT * FROM Gifdat")
        datetimes = [x.filename for x in gifdats]
        datetimes.sort()
#        self.response.headers['Content-Type'] = 'application/json'
#        self.response.write(json.dumps(datetimes))
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.write(",".join(datetimes))


class ShowGif(webapp2.RequestHandler):
    def get(self):
        logging.info("showgif : called : " + self.request.url)

        # Get date&time from URL.
        m = re.search("(?<=[\\\/])\d+(?=\.gif)", self.request.url)
        if m is None:
            self.error(404)
            return
        datetime = m.group(0)

        # Find gif from data store.
        query = "SELECT * FROM Gifdat WHERE filename = '" + datetime + "'"
        logging.info("showgif : query : " + query)
        gifdats = db.GqlQuery(query)

        # Show gif.
        for gifdat in gifdats:
            logging.info("showgif : gifdat : " + gifdat.filename)
            self.response.headers['Content-Type'] = 'image/gif'
            self.response.write(gifdat.content)
            break
        else:
            self.error(404)


app = webapp2.WSGIApplication([
                              ('/fetch', Fetchdata),
                              ('/datetimes', DateTimes),
                              ('/\d+.gif', ShowGif),
                              ],
                              debug=True)

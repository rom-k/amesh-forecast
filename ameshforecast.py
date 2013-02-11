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


class Giflist(db.Model):
    filenames = db.StringListProperty()


class Gifdat(db.Model):
    filename = db.StringProperty(required=True)
    content = db.BlobProperty()


class Fetchdata(webapp2.RequestHandler):
    def get(self):
        logging.debug("fetch : called : " + self.request.url)

        # Connect to tokyo-amesh site to get date&time list.
        amesh_url1 = "http://tokyo-ame.jwa.or.jp/scripts/mesh_index.js"
        result = urlfetch.fetch(amesh_url1)

        # Raise exception when access error occured.
        if result.status_code != 200:
            logging.warn("fetch : fetching amesh_url1 failed : " +
                         str(result.status_code))
            raise

        res = result.content.rstrip()
        logging.debug("fetch : result.content : " + res)

        # Use string.translate to remove chars except for "1234567890,".
        allchars = string.maketrans('', '')
        delchars = allchars.translate(allchars, string.digits + ',')
        amesh_datetimes = res.translate(allchars, delchars).split(",")

        # Get date&time list from datastore.
        giflists = db.GqlQuery("SELECT * FROM Giflist")
        giflist = giflists.get()
        try:
            db_datetimes = giflist.filenames
        except:
            giflist = Giflist()
            gifdats = db.GqlQuery("SELECT * FROM Gifdat")
            db_datetimes = [x.filename for x in gifdats]
        logging.debug("fetch : db_datetimes : " + ','.join(db_datetimes))

        # Add new gif to datastore if not added yet.
        for datetime in amesh_datetimes:
            if datetime in db_datetimes:
                continue

            logging.debug("fetch : add : " + datetime)
            db_datetimes.append(datetime)

            # Get gif image from tokyo amesh site.
            amesh_url2 = ("http://tokyo-ame.jwa.or.jp/mesh/100/" +
                          datetime + ".gif")
            result = urlfetch.fetch(amesh_url2)
            if result.status_code != 200:
                logging.warn("fetch : fetching amesh_url2 failed : " +
                             str(result.status_code))
                raise

            # Create new Gifdat instance and add to data store.
            gifdat = Gifdat(filename=datetime)
            gifdat.content = db.Blob(result.content)
            gifdat.put()

        # Keep data for 1 week = 60/5*24*7 data.
        db_datetimes.sort()
        discard = db_datetimes[2016:]
        db_datetimes = db_datetimes[:2016]

        # Remove old image from data store.
        gifdats = db.GqlQuery("SELECT * FROM Gifdat WHERE " +
                              "filename IN ('" + "','".join(discard) + "')")
        for gifdat in gifdats:
            datetime = gifdat.filename
            logging.debug("fetch : remove : " + datetime)
            gifdat.delete()

        # Store list
        giflist.filenames = db_datetimes
        giflist.put()


class DateTimes(webapp2.RequestHandler):
    def get(self):
        logging.debug("datetimes : called : " + self.request.url)

        # Output date&time list for gifdat.
        giflists = db.GqlQuery("SELECT * FROM Giflist")
        datetimes = giflists.get().filenames
        datetimes.sort()
#        self.response.headers['Content-Type'] = 'application/json'
#        self.response.write(json.dumps(datetimes))
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.write(",".join(datetimes))


class ShowGif(webapp2.RequestHandler):
    def get(self):
        logging.debug("showgif : called : " + self.request.url)

        # Get date&time from URL.
        m = re.search("(?<=[\\\/])\d+(?=\.gif)", self.request.url)
        if m is None:
            logging.warn("showgif : no matched datetime")
            self.error(404)
            return
        datetime = m.group(0)

        # Find gif from data store.
        query = "SELECT * FROM Gifdat WHERE filename = '" + datetime + "'"
        logging.debug("showgif : query : " + query)
        gifdats = db.GqlQuery(query)

        # Show gif.
        for gifdat in gifdats:
            logging.debug("showgif : gifdat : " + gifdat.filename)
            self.response.headers['Content-Type'] = 'image/gif'
            self.response.write(gifdat.content)
            break
        else:
            logging.warn("showgif : no matched datetime")
            self.error(404)


logging.getLogger().setLevel(logging.DEBUG)

app = webapp2.WSGIApplication([
                              ('/fetch', Fetchdata),
                              ('/datetimes', DateTimes),
                              ('/\d+.gif', ShowGif),
                              ],
                              debug=True)

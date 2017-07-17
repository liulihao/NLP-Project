"""
Get Reviews from Yelp by using Fusion API
(Revised from Yelp API example)
"""
from __future__ import print_function

import argparse
import json
import pprint
import requests
import sys
import urllib

import sys
import string
import csv
import requests
from lxml import html
from time import gmtime, strftime

# This client code can run on Python 2.x or 3.x.  Your imports can be
# simpler if you only need one of those.
try:
    # For Python 3.0 and later
    from urllib.error import HTTPError
    from urllib.parse import quote
    from urllib.parse import urlencode
except ImportError:
    # Fall back to Python 2's urllib2 and urllib
    from urllib2 import HTTPError
    from urllib import quote
    from urllib import urlencode


# OAuth credential placeholders that must be filled in by users.
# You can find them on
# https://www.yelp.com/developers/v3/manage_app
CLIENT_ID = 'cJ8UKGQy81X3Y5eTZ6syiA'
CLIENT_SECRET = 'qEbARWWGlhw1rHQvfC0AmOlfghOEZvsQHWBUYef5pwzY53CBXvWQf5SJBdQJdC55'

# API constants, you shouldn't have to change these.
API_HOST = 'https://api.yelp.com'
SEARCH_PATH = '/v3/businesses/search'
BUSINESS_PATH = '/v3/businesses/'  # Business ID will come after slash.
TOKEN_PATH = '/oauth2/token'
GRANT_TYPE = 'client_credentials'


# Defaults for our simple example.
DEFAULT_TERM = ''
DEFAULT_LOCATION = ''
DEFAULT_BUSINESS = ''
DEFAULT_RECENT = 0
SEARCH_LIMIT = 3


def obtain_bearer_token(host, path):
    """Given a bearer token, send a GET request to the API.
    Args:
        host (str): The domain host of the API.
        path (str): The path of the API after the domain.
        url_params (dict): An optional set of query parameters in the request.
    Returns:
        str: OAuth bearer token, obtained using client_id and client_secret.
    Raises:
        HTTPError: An error occurs from the HTTP request.
    """
    url = '{0}{1}'.format(host, quote(path.encode('utf8')))
    assert CLIENT_ID, "Please supply your client_id."
    assert CLIENT_SECRET, "Please supply your client_secret."
    data = urlencode({
        'client_id': CLIENT_ID,
        'client_secret': CLIENT_SECRET,
        'grant_type': GRANT_TYPE,
    })
    headers = {
        'content-type': 'application/x-www-form-urlencoded',
    }
    response = requests.request('POST', url, data=data, headers=headers)
    bearer_token = response.json()['access_token']
    return bearer_token


def request(host, path, bearer_token, url_params=None):
    """Given a bearer token, send a GET request to the API.
    Args:
        host (str): The domain host of the API.
        path (str): The path of the API after the domain.
        bearer_token (str): OAuth bearer token, obtained using client_id and client_secret.
        url_params (dict): An optional set of query parameters in the request.
    Returns:
        dict: The JSON response from the request.
    Raises:
        HTTPError: An error occurs from the HTTP request.
    """
    url_params = url_params or {}
    url = '{0}{1}'.format(host, quote(path.encode('utf8')))
    headers = {
        'Authorization': 'Bearer %s' % bearer_token,
    }

    print(u'Querying {0} ...'.format(url))

    response = requests.request('GET', url, headers=headers, params=url_params)

    return response.json()


def search(bearer_token, term, location):
    """Query the Search API by a search term and location.
    Args:
        term (str): The search term passed to the API.
        location (str): The search location passed to the API.
    Returns:
        dict: The JSON response from the request.
    """

    url_params = {
        'term': term.replace(' ', '+'),
        'location': location.replace(' ', '+'),
        'limit': SEARCH_LIMIT
    }
    return request(API_HOST, SEARCH_PATH, bearer_token, url_params=url_params)


def get_business(bearer_token, business_id):
    """Query the Business API by a business ID.
    Args:
        business_id (str): The ID of the business to query.
    Returns:
        dict: The JSON response from the request.
    """
    business_path = BUSINESS_PATH + business_id

    return request(API_HOST, business_path, bearer_token)


def query_api(term, location):
    """Queries the API by the input values from the user.
    Args:
        term (str): The search term to query.
        location (str): The location of the business to query.
    """
    bearer_token = obtain_bearer_token(API_HOST, TOKEN_PATH)

    response = search(bearer_token, term, location)

    businesses = response.get('businesses')

    if not businesses:
        print(u'No businesses for {0} in {1} found.'.format(term, location))
        return

    business_id = businesses[0]['id']

    print(u'{0} businesses found, querying business info ' \
        'for the top result "{1}" ...'.format(
            len(businesses), business_id))
    response = get_business(bearer_token, business_id)

    print(u'Result for business "{0}" found:'.format(business_id))
    pprint.pprint(response, indent=2)


def scrapingdata(business, recent):

    bearer_token = obtain_bearer_token(API_HOST, TOKEN_PATH)
    business_id = business
    linklist = []

    if(recent == 0):
        response = get_business(bearer_token, business_id)
        rc = int(response['review_count'])
    else:
        rc = int(recent)

    if(rc % 20 == 0):
        pages = rc/20
    else:
        pages = rc/20 + 1

    print(u'Result for business "{0}" found:'.format(business_id))
    print(u'Review Count: ' + str(rc))
    print(u'Number of pages for reviews: ' + str(pages))
    
    for i in xrange(0, pages):
        linklist.append('https://www.yelp.com/biz/'+business_id+'?start='+str(i*20)+'&sort_by=date_desc')

    with open('reviews/' + business + '.csv', 'wb') as csvfile:
        review_writer = csv.writer(csvfile, delimiter=',', quoting=csv.QUOTE_ALL)
        headrow = ['Author', 'Rating Value', 'Date Published', 'Description']
        review_writer.writerow(headrow)

        print('Start Writing Data...')
        for i in xrange(len(linklist)):
            
            print('Writing ' + str(i + 1) + '/' + str(len(linklist)) + ' page review...')
            page = requests.get(linklist[i])
            tags = html.fromstring(page.content)
            reviews = tags.xpath("//script[@type='application/ld+json']/text()")
            
            #text processing
            reviews[0] = reviews[0].replace(u'\xa0', ' ').encode('utf-8')
            reviews[0] = reviews[0].strip()
            reviews[0] = reviews[0].rstrip('\n')
            reviews_text = reviews[0]
            
            #text to JSON
            reviews_json = json.loads(reviews_text)

            #if(i == len(linklist) - 1):
            #    review_number = rc % 20
            #else:
            review_number = 20

            for i in xrange(0, review_number):
                datarow = []
                try:
                    ratingValue = str(reviews_json["review"][i]["reviewRating"]["ratingValue"])
                    datePublished = reviews_json["review"][i]["datePublished"]
                    description = reviews_json["review"][i]["description"].encode('utf-8').strip()
                    #print(description)
                    author = reviews_json["review"][i]["author"].encode('utf-8').strip()
                    datarow.append(author)
                    datarow.append(ratingValue)
                    datarow.append(datePublished)
                    datarow.append(description)
                    review_writer.writerow(datarow)
                except IndexError:
                    print('Completed')
                    sys.exit()
    print('Completed')

def main():
    parser = argparse.ArgumentParser()

    parser.add_argument('-q', '--term', dest='term', default=DEFAULT_TERM,
                        type=str, help='Search term (default: %(default)s)')

    parser.add_argument('-l', '--location', dest='location',
                        default=DEFAULT_LOCATION, type=str,
                        help='Search location (default: %(default)s)')

    parser.add_argument('-b', '--business', dest='business',
                        default=DEFAULT_BUSINESS, type=str,
                        help='Search business (default: %(default)s)')

    parser.add_argument('-r', '--recent', dest='recent',
                        default=DEFAULT_RECENT, type=str,
                        help='Get recent reviews (default: %(default)s)')


    input_values = parser.parse_args()

    try:
        #query_api(input_values.term, input_values.location, input_values.business)
        scrapingdata(input_values.business, input_values.recent)
    except HTTPError as error:
        sys.exit(
            'Encountered HTTP error {0} on {1}:\n {2}\nAbort program.'.format(
                error.code,
                error.url,
                error.read(),
            )
        )


if __name__ == '__main__':
    main()
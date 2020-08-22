# Spotify Sync

This project will sync the last X saved tracks on your spotify account into a dedicated playlist,
allowing you to only save this playlist on your phone instead of downloading all saved songs.

It's being developed live saturdays 16:30 CEST on my [Twitch channel](https://www.twitch.tv/RotesWasser).



## Stream Log

### Next Stream

* Implementing Synchronization

### Completed

#### Stream 2020-08-22
* Implemented creation of `SyncJob`s as well as their target playlists on Spotify via its API
* Added Spotify API Connection

#### Unscheduled Short Stream 2020-08-20
* Added `SyncJob`s to the Model, start of config UI

#### Off-Stream
* Switched to Postgres database
* Implemented OAuth token storage in the DB 

#### Stream 2020-08-15
* Initial project setup 
* Implemented OAuth-Login + user creation in the database

## TODO

* Synchronizing playlists
    * Notes
        * Use AuthorizedClientServiceOAuth2AuthorizedClientManager to access credentials
* Sync Configuration UI
    * Management of existing sync jobs
    * ~~Creation of sync jobs~~
* Handling of sync failures (e-mail on token expiry, etc.)
* Customized landing and login pages
    * Defining one will also fix the weird auto-login on protected controllers
* Containerization
* Monitoring instrumentation
* ~~Publishing (figuring out ToS/GDPR/imprint. Fun!)~~
  * After careful consideration, I decided that I will not host this application because of GDPR. In my eyes, it's too
  much of a risk to take on as an individual, especially since this application will never generate revenue
  that could balance potential legal fees if I were to screw up some obscure compliance part. Instead, I decided
  to license it under the AGPLv3 so that you are free to host it yourselves for now. Maybe the law changes in the
  future, so that I can host it for you. I'm sorry, [please understand](https://www.youtube.com/watch?v=F535Xpu0NDE).
  
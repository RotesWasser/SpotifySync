# Spotify Sync

This project will sync the last X saved tracks on your spotify account into a dedicated playlist,
allowing you to only save this playlist on your phone instead of downloading all saved songs.

It's being developed live on stream saturdays 16:30 CEST on my Twitch channel.

## Stream Log

### Planned

* Synchronizing playlists
    * Notes
        * Use AuthorizedClientServiceOAuth2AuthorizedClientManager to access credentials
* Sync Configuration UI
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
  
#### Next Stream

* Figuring out task scheduling
* Getting to know the Spotify API

### Completed

### Unscheduled Short Stream 2020-08-20
* Add `SyncJob`s to the Model, start of config UI

#### Off-Stream
* Switch to Postgres database
* Store OAuth tokens in the DB 

#### Stream 2020-08-15
* Initial project setup 
* OAuth-Login + user creation in the database
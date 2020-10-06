# Spotify Sync

This project will sync the last X liked songs on your spotify account into a dedicated playlist,
allowing you to only save this playlist on your phone instead of downloading all liked songs.

## TODO
* Move actual sync behind a queue of sync requests
* Sync Configuration UI
    * Allow pausing existing sync jobs
    * Manual trigger of a sync
    * ~~Creation of sync jobs~~
* Handling of sync failures (e-mail on token expiry, etc.)
* Customized landing and login pages
* CSRF Protection
* Sanitization of Data displayed in the config UI originating from Spotify
    * Prevent XSS and other injections
* Increasing robustness
    * Handling rate-limits, including dynamically finding a playlist update rate that the 
    Spotify API is comfortable with
    * Generally increased fault tolerance
* Containerization
* Monitoring instrumentation
* Supporting playlists as sync source?
* ~~Publishing (figuring out ToS/GDPR/imprint. Fun!)~~
  * After careful consideration, I decided that I will not host this application because of GDPR. In my eyes, it's too
  much of a risk to take on as an individual, especially since this application will never generate revenue
  that could balance potential legal fees if I were to screw up some obscure compliance part. Instead, I decided
  to license it under the AGPLv3 so that you are free to host it yourselves for now. Maybe the law changes in the
  future, so that I can host it for you. I'm sorry, [please understand](https://www.youtube.com/watch?v=F535Xpu0NDE).
  
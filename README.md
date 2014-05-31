# Blogger-me #

---
A very simple android demo application which uses the Google Blogger API v3 to allow a user to connect to his/her blogger account and do simple postings and viewing of his/her blog.

## Features
Allows user to post a blog 
Allows user to view the posts
Allows saving of multiple drafts

**Note:**  
1. Take note that if the user exits the application while editing a post, there's no guarantee that the post will still be there when user goes back into the app. So it would be better to post it in one shot (Until saving in draft feature is added).
2. Developers have to add their own keys in the clientcredentials class, as described in the class itself.

##Roadmap
* Allows saving of draft to blogger
* Allowing the adding of images and links into the posts
* Add database implementation to allow saving of posts, tags and blogs information. Allows for faster loading
* Adding drag down to refresh implementation to allow sync-ing of database to blog
* Adding full-resync function in menu to clear whole database and re-sync from blogger

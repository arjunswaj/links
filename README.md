#Links#
A free, open-sourced online bookmarking service which has the following features
  

 - Bookmarking URLs
 - Searching among the saved bookmarks
 - Creating groups and sharing bookmarks
 - REST APIs for consumption byf third party  client
 - Deployment on a private server
 - Extension of installation through plugins


----------
####[Links API documentation](https://bitbucket.org/linkiiitb/links/wiki/Links%20API%20documentation) ####
####[How to create plugins](https://bytebucket.org/linkiiitb/links/raw/dd93805750416576a9832bc77561546381a41d14/documentation/Design/Plugin%20Architecture/PluginArchitecture.pdf)
####[To do](https://bitbucket.org/linkiiitb/links/issues?status=new&status=open)####

----------
## Installation(https://bitbucket.org/linkiiitb/links/issue/24/setup-process)

  * Clone the Links application application directory from this git repository. Navigate to the same.
  * Install `mysql`. Modify the links/links/links/config/database.yml with the database details(name, username, and password) where you wish for Links to store its data.
  * Install and setup `rbenv` and `ruby` as illustrated [here](https://github.com/sstephenson/rbenv#installation)
  * Install `rails` : `gem install rails`
  * Install required gems : `bundle install`
  * Migrate database : `rails db:migrate`
  * Navigate to links/links and run the installation script `install.sh` to complete the installation.
  * Run a server which has an application container for RoR like Phusion Passenger, configure the same and run it.
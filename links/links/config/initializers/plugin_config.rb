require 'yaml'
PLUGIN_CONFIG = YAML.load_file("#{Rails.root}/config/plugin_config.yml")[Rails.env]
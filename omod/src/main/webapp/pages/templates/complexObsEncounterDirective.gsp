<style>
</style>

<i ng-show="!thumbnailCfg" class="icon-spinner icon-spin icon-2x pull-left"></i>
<vdui-thumbnail ng-show="thumbnailCfg" ng-repeat="obs in allObs" obs="obs" config="thumbnailCfg"></vdui-thumbnail>
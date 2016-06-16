<style>
</style>

<i ng-hide="thumbConfig" class="icon-spinner icon-spin icon-2x pull-left"></i>
<vdui-thumbnail ng-show="thumbConfig" ng-repeat="obs in allObs" obs="obs" config="thumbConfig"></vdui-thumbnail>
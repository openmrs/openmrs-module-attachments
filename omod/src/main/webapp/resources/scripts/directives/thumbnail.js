angular.module('vdui.widget.thumbnail', ['vdui.service.complexObsService', 'vdui.service.complexObsCacheService', 'ngDialog', 'vdui.widget.modalImage'])

.config(['$compileProvider', function ($compileProvider) {
  /* Prevent Angular from throwing error when querying images using 'data' protocol */
  $compileProvider.imgSrcSanitizationWhitelist(/^\s*(https?|local|data):/);
}])

.directive('vduiEnterKeyDown', function() {
  return function(scope, element, attrs) {
    element.bind("keydown keypress", function(event) {
      if(event.which === 13) {
        scope.$apply(function() {
          scope.$eval(attrs.vduiEnterKeyDown, {'event': event});
        });
        event.preventDefault();
      }
    });
  };
})

.directive('vduiEscapeKeyDown', function() {
  return function(scope, element, attrs) {
    element.bind("keydown keypress", function(event) {
      if(event.which === 27) {
        scope.$apply(function() {
          scope.$eval(attrs.vduiEscapeKeyDown, {'event': event});
        });
        event.preventDefault();
      }
    });
  };
})

.directive('vduiThumbnail', ['ComplexObs', 'ComplexObsCacheService', 'ngDialog', '$http', '$window', '$sce', function(Obs, obsCache, ngDialog, $http, $window, $sce) {
  return {
    restrict: 'E',
    scope: {
      obs: '=',
      config: '='
    },
    templateUrl: '/' + module.getPath(OPENMRS_CONTEXT_PATH) + '/templates/thumbnail.page',

    controller: function($scope) {

      var msgs = [
        module.getProvider() + ".thumbail.get.error",
        module.getProvider() + ".thumbail.save.success",
        module.getProvider() + ".thumbail.save.error",
        module.getProvider() + ".thumbail.delete.success",
        module.getProvider() + ".thumbail.delete.error"
      ]
      emr.loadMessages(msgs.toString());

      $scope.canEdit = function() {
        if($scope.config.canEdit) {
          if($scope.config.canEdit === true) {
            return true;
          }
        }
        return false;
      }

      $scope.toggleVisible = function(visible) {
        $scope.active = visible;
      }

      $scope.toggleEditMode = function(editMode) {
        $scope.typedText = {};
        if ($scope.canEdit()) {
          $scope.typedText.newCaption = $scope.obs.comment;
          $scope.editMode = editMode;
          if ($scope.editMode) {
            $scope.editModeCss = "vdui_thumbnail-edit-mode";
          }
          else {
            $scope.editModeCss = "";
          }
        }
      }

      $scope.toggleEditMode(false);
      $scope.toggleVisible(true);
      $scope.src = "";
      $scope.loading = false;

      $scope.getEditModeCss = function() {
        return $scope.editModeCss;
      }

      $scope.getPrettyDate = function() {
        var timeFormat = "DD MMM YY";
        var now = new Date();
        var obsDate = moment($scope.obs.obsDatetime).toDate();  // new Date(..) would throw 'Invalid date' on Apple WebKit
        if (obsDate.toDateString() === now.toDateString()) {
          timeFormat = "HH:mm";
        }
        else {
          if (obsDate.getYear() === now.getYear()) {
            timeFormat = "DD MMM";
          }
        }
        return moment(obsDate).format(timeFormat);
      }

      $scope.saveCaption = function() {
        var caption = $scope.obs.comment;
        if ((caption == $scope.typedText.newCaption) || ($scope.typedText.newCaption == "" && !$scope.config.allowNoCaption)) {
          $scope.toggleEditMode(false);
          return;
        }

        $scope.obs.comment = $scope.typedText.newCaption;

        var saved = Obs.save({
          uuid: $scope.obs.uuid,
          comment: $scope.obs.comment
        });
        saved.$promise.then(function(obs) {
          $scope.toggleEditMode(false);
          $().toastmessage('showToast', { type: 'success', position: 'top-right', text: emr.message(module.getProvider() + ".thumbail.save.success") });
        }, function(reason) {
          $scope.obs.comment = caption;
          $().toastmessage('showToast', { type: 'error', position: 'top-right', text: emr.message(module.getProvider() + ".thumbail.save.error") });
          console.log(err);
        });
      }

      $scope.confirmDelete = function() {
        // https://github.com/likeastore/ngDialog/blob/master/README.md
        ngDialog.open({
          template: 'vdui_thumbnail-confirm-dialog',
          scope: $scope,
          controller: ['$scope', function($scope) {
            $scope.showSpinner = false;
            $scope.confirm = function() {
              $scope.showSpinner = true;
              $scope.purge(true, $scope);
            }
          }]
        });
      }

      $scope.purge = function(purge, scope) {
        Obs.delete({
          uuid: scope.obs.uuid,
          purge: purge
        })
        .$promise.then(function(res) {
          scope.toggleVisible(false);
          scope.closeThisDialog();
          $().toastmessage('showToast', { type: 'success', position: 'top-right', text: emr.message(module.getProvider() + ".thumbail.delete.success") });
        }, function(err) {
          scope.closeThisDialog();
          if (purge === true) { // We should only do this if error 500 is the cause: https://github.com/openmrs/openmrs-core/blob/1.11.x/api/src/main/java/org/openmrs/api/impl/ObsServiceImpl.java#L213
            scope.purge(null, scope);
          }
          else {
            $().toastmessage('showToast', { type: 'error', position: 'top-right', text: emr.message(module.getProvider() + ".thumbail.delete.error") });  
            console.log(err);
          }
        }); 
      }

      /*
        Injects the icon's HTML into the DOM. We had to avoid an in-DOM ng-if (or ng-switch) due to performance issues.
      */
      var setIconHtml = function(complexObs) {
        var html = "";
        switch (complexObs.contentFamily) {
          case module.family.IMAGE:
            html =  '<img src="' +
                    'data:' + complexObs.mimeType + ';base64,' + module.arrayBufferToBase64(complexObs.complexData) +
                    '"/>';
            break;

          case module.family.PDF:
            html =  '<i class="icon-file-pdf-o"></i>' +
                    '<span class="vdui_thumbnail-extension">' + complexObs.contentFamily.toUpperCase() + '</span>';
            break;

          case module.family.OTHER:
          default:
            html =  '<i class="icon-file"></i>' +
                    '<span class="vdui_thumbnail-extension">.' + complexObs.fileExt + '</span>';
            break;
        }
        $scope.iconHtml = $sce.trustAsHtml(html);
      }

      $scope.init = function() {
        $scope.displayDefaultContentFamily = true;

        obsCache.getComplexObs($scope.obs, $scope.config.downloadUrl, $scope.config.thumbView)
        .then(function(res) {
          $scope.loading = false;
          $scope.obs.complexData = res.complexData; // Turning the obs into a complex obs.
          setIconHtml($scope.obs);
        }, function() {
          $scope.loading = false;
          $().toastmessage('showToast', { type: 'error', position: 'top-right', text: emr.message(module.getProvider() + ".thumbail.get.error") });
          console.log(err);
        });
      }

      $scope.displayContent = function() {
        var win = getWindow($scope.obs.contentFamily);

        $scope.loading = true;
        obsCache.getComplexObs($scope.obs, $scope.config.downloadUrl, $scope.config.originalView)
        .then(function(res) {
          $scope.loading = false;
          switch ($scope.obs.contentFamily) {
            case module.family.IMAGE:
              displayImage($scope.obs, res.complexData);
              break;

            case module.family.PDF:
              displayPdf($scope.obs, res.complexData, win);
              break;

            case module.family.OTHER:
            default:
              displayOther($scope.obs, res.complexData);
              break;
          }
        }, function() {
          $scope.loading = false;
          $().toastmessage('showToast', { type: 'error', position: 'top-right', text: emr.message(module.getProvider() + ".thumbail.get.error") });
          console.log(err);
        });
      }

      var displayImage = function(obs, data) {
        $scope.imageConfig = {};
        $scope.imageConfig.bytes = module.arrayBufferToBase64(data);
        $scope.imageConfig.mimeType = obs.mimeType;
        $scope.imageConfig.caption = obs.comment;
      }

      var displayPdf = function(obs, data, win) {
        var blob = new Blob([data], {type: obs.mimeType});
        var blobUrl = URL.createObjectURL(blob);
        win.location.href = blobUrl;
      }

      var displayOther = function(obs, data) {   // http://stackoverflow.com/a/28541187/321797
        var blob = new Blob([data], {type: obs.mimeType});
        var downloadLink = angular.element('<a></a>');
        downloadLink.attr('href', $window.URL.createObjectURL(blob));
        downloadLink.attr('download', obs.fileName);
        downloadLink[0].click();
      }

      var getWindow = function(contentFamily) {
        switch ($scope.obs.contentFamily) {
          case module.family.PDF:
          return $window.open('');
          default:
          return {};
        }
      }
    }
  };
}]);
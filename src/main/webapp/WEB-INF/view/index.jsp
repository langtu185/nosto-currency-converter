<!DOCTYPE html>
<html lang="en">

<head>
	<meta charset="utf-8" />
	<title>Exchange Converter</title>
	<!-- Latest compiled Bootstrap CSS -->
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.1/dist/css/bootstrap.min.css" rel="stylesheet"
		crossorigin="anonymous">

	<!--Latest jQuery -->
	<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js" crossorigin="anonymous"></script>

	<!-- Latest compiled Boostrap -->
	<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.1/dist/js/bootstrap.bundle.min.js"
		crossorigin="anonymous"></script>

	<meta name="_csrf" content="${_csrf.token}" />
	<meta name="_csrf_header" content="${_csrf.headerName}" />
</head>

<body>
	<div id="main-container" style="margin: auto; margin-top:100px; width: 75%;">
		<div class="jumbotron p-4 mb-5" style="background-color: #dddd02;">
			<h1 style="font-size: 4.5rem; text-align: center; color: #411212;">EXCHANGE CONVERTER</h1>
		</div>

		<div class="jumbotron p-4" style="background-color: #e9ecef;">
			<!-- Money Exchange Form -->
			<form id="money-exchange-form" class="mt-3 needs-validation" novalidate>
				<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
				<div class="row gx-3 justify-content-center">
					<div class="col-3">
						<div class="input-group has-validation">
							<input required class="form-control" type="number" placeholder="Value" name="value" min="0">
							<div class="invalid-feedback">
								Please fill in a non-negative number
							</div>
						</div>
					</div>
					<div class="col-4">
						<div class="input-group has-validation">
							<input class="form-control" type="text" required name="source" pattern="[a-zA-Z]{3}" minlength="3" maxlength="3" autocapitalize="characters" placeholder="Source Currency (E.g. EUR)" oninput="this.value = this.value.toUpperCase()">
							<div class="invalid-feedback">
								Please fill in a three characters source currency.
							</div>
						</div>
					</div>
					<div class="col-4">
						<input class="form-control" type="text" required name="target" pattern="[a-zA-Z]{3}" minlength="3" maxlength="3" autocapitalize="characters" placeholder="Target Currency (E.g. USD)" oninput="this.value = this.value.toUpperCase()">
						<div class="invalid-feedback">
							Please fill in a three characters target currency.
						</div>
					</div>
				</div>
				<div class="row mt-4 justify-content-center">
					<button id="convert-btn" class="col-8 btn btn-default btn-primary" type="submit">
						Convert
					</button>
				</div>
			</form>

			<!-- Result here-->
			<div class="mt-3">
				<p id="err-msg" class="mx-auto badge alert alert-danger"
					style="display: none; white-space: normal; width: 30%;">Error Here</p>
				<p id="success-msg" class="mx-auto badge alert alert-success"
					style="display: none; white-space: normal; width: 30%">Success Here</p>
			</div>
		</div>
	</div>

	<script>
		(function () {
			let form = document.getElementById('money-exchange-form');

			form.addEventListener('submit', (event) => {
				// Hide error and success message
				const $errEl = $('#err-msg');
				const $successEl = $('#success-msg');
				$errEl.hide();
				$successEl.hide();

				// Disble convert button
				const $convertBtn = $('#convert-btn');
				$convertBtn.attr('disabled', true)

				if (!form.checkValidity()) {
						event.preventDefault();
						event.stopPropagation();
						form.classList.add('was-validated');
						$convertBtn.removeAttr('disabled');
					} else {
						event.preventDefault();
						event.stopPropagation();

						// Get token and header
						const token = $('meta[name="_csrf"]').attr("content");
						const header = $('meta[name="_csrf_header"]').attr("content");

						// Get data
						const dataArr = $('#money-exchange-form').serializeArray();
						let data = {};
						dataArr.forEach((item) => { data[item.name] = item.value.trim() });

						$.ajax({
							method: 'POST',
							url: '/api/convert',
							data: JSON.stringify(data),
							contentType: 'application/json;charset=utf-8',
							dataType: 'json',
							headers: {
										'X-CSRF-TOKEN':token,
								},
							success: (res) => {
								const {value, localize, currency} = res;

								// Display success message
								$successEl
									.text(Intl.NumberFormat(localize, {style: 'currency', currency: currency})
									.format(value));
								$successEl.css('display', 'block');
							},
							error: ($xhr, status, err) => {
								let errMsg = '';

								if (!$xhr.responseJSON && err) {
									errMsg = err
								} else {
									const {error, errors} = $xhr.responseJSON;

									if (errors && errors[0].defaultMessage) {
										errMsg = errors[0].defaultMessage;
									} else {
										errMsg = error;
									}
								}

								// Display error message
								$errEl.text(errMsg);
								$errEl.css('display', 'block');
							},
							complete: () => {
								// Enable convert button
								$convertBtn.removeAttr('disabled');
							}
						})
					}
			}, false)
		})()
	</script>
</body>

</html>

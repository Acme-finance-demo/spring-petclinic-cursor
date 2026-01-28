$(document).ready(function() {
  let selectedDate = null;

  // Initialize Datepicker
  $('#datepicker').datepicker({
    dateFormat: 'yy-mm-dd',
    onSelect: function(dateText) {
      selectedDate = dateText;
      loadAppointments(dateText);
      updateSelectedDateDisplay(dateText);
    }
  });

  // Load appointments for selected date
  function loadAppointments(date) {
    $.ajax({
      url: '/appointments/by-date',
      type: 'GET',
      data: { date: date },
      success: function(appointments) {
        displayAppointments(appointments);
      },
      error: function() {
        $('#appointmentsList').html('<li class="no-appointments">予約の読み込みに失敗しました</li>');
      }
    });
  }

  // Display appointments in list
  function displayAppointments(appointments) {
    const $list = $('#appointmentsList');
    $list.empty();

    if (appointments.length === 0) {
      $list.html('<li class="no-appointments">この日付に予約はありません</li>');
      return;
    }

    appointments.forEach(function(appointment) {
      const time = formatTime(appointment.dateTime);
      const statusClass = 'status-' + (appointment.status || 'scheduled').toLowerCase();
      const statusText = getStatusText(appointment.status);

      const $item = $('<li class="appointment-item"></li>')
        .html(`
          <div class="appointment-time">${time}</div>
          <div class="appointment-description">${appointment.description}</div>
          <span class="appointment-status ${statusClass}">${statusText}</span>
        `)
        .click(function() {
          showAppointmentDetail(appointment);
        });

      $list.append($item);
    });
  }

  // Show appointment detail in modal
  function showAppointmentDetail(appointment) {
    const dateTime = formatDateTime(appointment.dateTime);
    const petName = appointment.pet ? appointment.pet.name : 'N/A';
    const vetName = appointment.vet ? (appointment.vet.firstName + ' ' + appointment.vet.lastName) : 'N/A';
    const statusText = getStatusText(appointment.status);

    $('#modalDateTime').text(dateTime);
    $('#modalPetName').text(petName);
    $('#modalVetName').text(vetName);
    $('#modalDescription').text(appointment.description);
    $('#modalStatus').text(statusText);

    $('#appointmentModal').modal('show');
  }

  // Update selected date display
  function updateSelectedDateDisplay(dateText) {
    const date = new Date(dateText);
    const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
    const formattedDate = date.toLocaleDateString('ja-JP', options);
    $('#selectedDate').text(formattedDate);
  }

  // Format time from LocalDateTime string
  function formatTime(dateTimeString) {
    const date = new Date(dateTimeString);
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  // Format full date and time
  function formatDateTime(dateTimeString) {
    const date = new Date(dateTimeString);
    const options = { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' };
    return date.toLocaleDateString('ja-JP', options);
  }

  // Get status text in Japanese
  function getStatusText(status) {
    const statusMap = {
      'SCHEDULED': '予約済み',
      'COMPLETED': '完了',
      'CANCELLED': 'キャンセル'
    };
    return statusMap[status] || status;
  }
});
import React, { useState } from 'react'

const PhoneSlider = () => {
    const [phoneNumber, setPhoneNumber] = useState<string>('7000000000')
  return (
    <div>
        <h1>Enter your phone number:</h1>
        <input type="range" min="7000000000" max="7999999999" value={phoneNumber} onChange={e => setPhoneNumber(e.target.value)} className="slider" id="myRange"  />
        <h2>0{phoneNumber}</h2>
    </div>
  )
}

export default PhoneSlider
